# Delta AI 客服系统 - Grafana 监控仪表盘部署方案

> @author 刘建国 | 版本 1.0 | 2026-05-10

---

## 一、架构概览

```
┌─────────────────────────────────────────────────────────┐
│                     Docker Compose                        │
│                                                          │
│  ┌──────────┐    ┌──────────┐    ┌──────────────────┐   │
│  │   MySQL   │    │  Redis   │    │   Backend (:8080) │   │
│  │  (:3306)  │    │ (:6379)  │    │ /actuator/       │   │
│  └────┬─────┘    └────┬─────┘    │   prometheus ◄────┼───│─── Prometheus 采集
│       │               │          └────────┬─────────┘   │
│       │               │                   │              │
│       └───────┬───────┘                   │              │
│               │                           │              │
│        ┌──────▼──────┐            ┌───────▼───────┐     │
│        │   Nginx     │            │  Prometheus    │     │
│        │  (:80:443)  │            │  (:9090)       │     │
│        └─────────────┘            └───────┬───────┘     │
│                                           │              │
│                                    ┌──────▼───────┐     │
│                                    │   Grafana     │     │
│                                    │  (:3000)      │     │
│                                    │  Dashboard    │     │
│                                    └──────────────┘     │
│                                                          │
│  网络: delta-network (bridge driver)                      │
└─────────────────────────────────────────────────────────┘
```

**数据流**：`Backend (Micrometer) → /actuator/prometheus → Prometheus (采集) → Grafana (可视化)`

---

## 二、新增配置文件

### 2.1 文件清单

| 文件路径 | 用途 |
|---------|------|
| `config/prometheus.yml` | Prometheus 采集配置（Scrape目标、采集间隔） |
| `config/grafana-datasources.yml` | Grafana 数据源预配置（Prometheus、可选Loki） |
| `config/grafana-dashboards/default.yml` | Grafana 仪表盘自动加载器配置 |
| `config/grafana-dashboards/delta-ai-dashboard.json` | 性能监控仪表盘 JSON（3面板组8面板） |

### 2.2 Prometheus 配置说明

```yaml
# config/prometheus.yml
global:
  scrape_interval: 15s           # 每15秒采集一次指标
  evaluation_interval: 30s       # 评估规则每30秒执行

scrape_configs:
  - job_name: "delta-backend"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["backend:8080"]  # 通过 Docker 网络名称访问
```

**关键参数**：
- `scrape_interval: 15s` — 平衡数据精度与存储开销
- `storage.tsdb.retention.time: 30d` — 保留30天历史数据（docker-compose 命令行参数）
- `web.enable-lifecycle` — 允许热重载配置 `curl -X POST http://localhost:9090/-/reload`

### 2.3 Grafana 自动加载机制

Grafana 使用 **Provisioning** 机制实现无感部署：

1. **数据源自动加载**：`grafana-datasources.yml` → 容器内 `/etc/grafana/provisioning/datasources/`
2. **仪表盘自动加载**：`default.yml` 指定扫描 `/etc/grafana/provisioning/dashboards/` 目录下所有 `.json` 文件
3. **仪表盘 JSON** 中所有面板直接引用 UID `prometheus-delta`，无需手动导入

```
启动流程:
  docker compose up -d grafana
    → Grafana 启动
    → 自动读取 datasources.yml → 创建名为 "Prometheus" 的数据源 (UID: prometheus-delta)
    → 自动扫描 dashboards/*.json → 导入到 "Delta AI 客服系统" 目录
    → 仪表盘即开即用
```

---

## 三、Docker Compose 新增服务

### Prometheus 服务

| 参数 | 值 |
|------|-----|
| 镜像 | `prom/prometheus:v2.52.0` |
| 容器名 | `delta-prometheus` |
| 端口映射 | `9090:9090` |
| 配置文件 | `./config/prometheus.yml` (只读挂载) |
| 数据卷 | `prometheus_data:/prometheus` (时序数据持久化) |
| 数据保留 | 30天 |
| 依赖服务 | backend (先启动后端再开始采集) |

### Grafana 服务

| 参数 | 值 |
|------|-----|
| 镜像 | `grafana/grafana:10.4.1` |
| 容器名 | `delta-grafana` |
| 端口映射 | `3000:3000` |
| 管理员用户 | `admin` (通过 `GRAFANA_ADMIN_USER` 环境变量) |
| 管理员密码 | `admin123` (通过 `GRAFANA_ADMIN_PASSWORD` 环境变量) |
| 数据卷 | `grafana_data:/var/lib/grafana` (仪表盘、用户、设置持久化) |
| 依赖服务 | prometheus |
| 禁止注册 | `GF_USERS_ALLOW_SIGN_UP=false` |

---

## 四、部署步骤

### 4.1 前置条件

- Docker Engine 20.10+
- Docker Compose v2.x
- Backend 镜像已构建（`delta-admin:latest`）
- Backend 已启用 `/actuator/prometheus` 端点（当前已配置 ✅）

### 4.2 部署命令

```bash
# 1. 构建后端镜像（如未构建）
cd d:/Project/AI-SERVERS
docker build -t delta-admin:latest .

# 2. 启动全部服务（包括 Prometheus + Grafana）
docker compose up -d

# 3. 验证服务状态
docker compose ps

# 预期输出（7个服务全部 healthy/up）：
#   NAME               STATUS
#   delta-mysql        healthy
#   delta-redis        healthy
#   delta-backend      running
#   delta-prometheus   running
#   delta-grafana      running
#   delta-nginx        running
```

### 4.3 仅启动监控服务

```bash
# 如果后端已在运行，仅启动监控栈
docker compose up -d prometheus grafana
```

---

## 五、验证方法

### 5.1 Prometheus 验证

```bash
# 1. 检查 Prometheus Target 状态
# 浏览器打开: http://localhost:9090/targets
# 预期: delta-backend 状态为 UP，last scrape 在15秒以内

# 2. 命令行验证
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets'

# 3. 测试指标查询
curl "http://localhost:9090/api/v1/query?query=process_cpu_usage"
```

**关键检查点**：
- [ ] `http://localhost:9090/targets` 中 `delta-backend` State 为 **UP**
- [ ] `http://localhost:9090/graph` 输入 `jvm_memory_used_bytes` 有数据返回
- [ ] 无 "context deadline exceeded" 错误

### 5.2 Backend 端点验证

```bash
# 确认 Backend 正在暴露 Prometheus 指标
curl http://localhost:8080/actuator/prometheus

# 预期返回类似:
#   process_cpu_usage 0.05
#   jvm_memory_used_bytes{area="heap"} 1.234e8
#   http_server_requests_seconds_count{uri="/api/xxx"} 42
```

### 5.3 Grafana 验证

```bash
# 1. 浏览器打开 Grafana: http://localhost:3000
# 2. 使用管理员账号登录: admin / admin123
# 3. 左侧菜单 → "Dashboards" → 应出现 "Delta AI 客服系统" 目录
# 4. 点击 "Delta AI 客服系统 - 性能监控仪表盘"
```

**关键检查点**：
- [ ] 登录后左侧 → **Connections → Data sources** → 看到 `Prometheus` 且状态为绿色 ✅
- [ ] 左侧 → **Dashboards** → `Delta AI 客服系统` 目录下有预配置仪表盘
- [ ] 仪表盘中 **3个面板组**（应用性能总览、业务指标、AI模型健康）共 **8个面板** 全部显示数据
- [ ] CPU 使用率、JVM 堆内存仪表盘显示非零数值
- [ ] HTTP 请求速率时序图有趋势线
- [ ] AI 模型健康状态显示 "UP"（绿色背景）

### 5.4 常见问题排查

| 现象 | 原因 | 解决方案 |
|------|------|---------|
| Prometheus target 显示 DOWN | Backend 未启动或网络不通 | `docker compose ps` 检查 backend 状态；确认 network 为 `delta-network` |
| Grafana 面板无数据 | 数据源 UID 不匹配 | 确认面板中 `uid` 为 `prometheus-delta`，与 `grafana-datasources.yml` 一致 |
| 仪表盘未自动导入 | dashboard JSON 路径不对 | 确认文件在 `config/grafana-dashboards/` 目录且名为 `.json` |
| 指标 query 返回空 | Spring Boot 未启用 prometheus 端点 | 确认 `application-docker.yml` 中 `management.endpoints.web.exposure.include` 包含 `prometheus` |

---

## 六、仪表盘功能说明

### 面板组1：应用性能总览

| 面板 | 指标 | PromQL | 类型 |
|------|------|--------|------|
| CPU 使用率 | `process_cpu_usage` | `process_cpu_usage{application="delta-ai-customer-service"} * 100` | Gauge |
| JVM 堆内存 | `jvm_memory_used_bytes` | `jvm_memory_used_bytes{area="heap"}` | Gauge |
| HTTP 请求速率 | `http_server_requests_seconds_count` | `rate(http_server_requests_seconds_count[5m])` | TimeSeries |

### 面板组2：业务指标

| 面板 | 指标 | PromQL | 类型 |
|------|------|--------|------|
| 消息总量 | `messages_total` | 直接查询 | Stat |
| 订单总量 | `orders_total` | 直接查询 | Stat |
| 活跃客户数 | `active_customers_total` | 直接查询 | Stat |

### 面板组3：AI 模型健康

| 面板 | 指标 | PromQL | 类型 |
|------|------|--------|------|
| AI 模型健康状态 | `health` | `health{name="aiModel"}` | Stat（值映射） |
| AI 接口响应时间 | `http_server_requests_seconds` | 过滤 `uri="/api/ai/*"` | TimeSeries |

---

## 七、后续扩展建议

1. **告警规则**：在 `config/` 目录添加 `prometheus-alerts.yml`，配置 CPU > 80% / 内存 > 90% / 服务 DOWN 等告警
2. **Loki 日志聚合**：取消 `grafana-datasources.yml` 中 Loki 注释，部署 Loki 后实现指标→日志联动
3. **Grafana Alerting**：在 Grafana 内配置告警规则，支持 Email/Slack/企业微信通知
4. **自定义业务指标**：在 Java 代码中通过 `MeterRegistry` 注册更多业务指标
5. **Nginx 反向代理**：将 Prometheus 和 Grafana 通过 Nginx 代理对外暴露（配置 HTTPS + 基础认证）
6. **持久化存储优化**：考虑将 Prometheus/Grafana 数据迁移到外部存储卷

---

## 八、文件变更汇总

| 操作 | 文件路径 |
|------|---------|
| 新增 | `config/prometheus.yml` |
| 新增 | `config/grafana-datasources.yml` |
| 新增 | `config/grafana-dashboards/default.yml` |
| 新增 | `config/grafana-dashboards/delta-ai-dashboard.json` |
| 修改 | `docker-compose.yml` (添加 prometheus + grafana 服务) |
| 保留 | `docs/deploy/grafana-dashboard.json` (原始备份) |