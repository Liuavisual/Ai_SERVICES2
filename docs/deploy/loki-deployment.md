# Delta AI 客服系统 - Loki + Promtail 日志收集部署方案

> @author 刘建国 | 版本 1.0 | 2026-05-10

---

## 一、架构概览

```
┌──────────────────────────────────────────────────────────────────┐
│                        Docker Compose                             │
│                                                                   │
│  ┌──────────┐   ┌──────────┐   ┌──────────────────┐              │
│  │  MySQL    │   │  Redis   │   │  Backend (:8080)  │              │
│  │  (:3306)  │   │ (:6379)  │   │  /app/logs/        │              │
│  └────┬─────┘   └────┬─────┘   │    delta-ai.json ◄──┼──── 采集    │
│       │              │         └────────┬─────────┘   │   JSON日志 │
│       │              │                  │              │           │
│  ┌────▼──────┐  ┌────▼──────┐  ┌───────▼───────┐  ┌──▼─────────┐ │
│  │  Nginx    │  │ Prometheus │  │  Promtail     │  │   Loki     │ │
│  │ (:80:443) │  │  (:9090)   │  │  (:9080)      │  │  (:3100)   │ │
│  │ access.log│  │            │  │  采集+推送 ────┼──►  存储+查询 │ │
│  └────┬──────┘  └─────┬──────┘  └──────────────┘  └─────┬──────┘ │
│       │               │                                  │        │
│       │         ┌─────▼──────┐                           │        │
│       │         │  Grafana   │◄──────────────────────────┘        │
│       │         │  (:3000)   │   LogQL 查询 + 可视化              │
│       │         └────────────┘                                    │
│                                                                   │
│  网络: delta-network (bridge driver)                               │
└──────────────────────────────────────────────────────────────────┘
```

**数据流**：`Backend (JSON文件) / Docker (stdout/stderr) → Promtail (采集/解析) → Loki (存储/索引) → Grafana (查询/可视化)`

---

## 二、新增配置文件

### 2.1 文件清单

| 文件路径 | 用途 | 关键参数 |
|---------|------|---------|
| `config/loki-config.yml` | Loki 日志聚合服务配置 | 端口3100、30天保留、boltdb-shipper存储 |
| `config/promtail-config.yml` | Promtail 日志采集Agent配置 | Docker容器日志、JSON文件日志、Nginx日志 |
| `config/grafana-datasources.yml` | Grafana 数据源（已修改） | 启用 Loki 数据源 (UID: loki-delta) |

### 2.2 Loki 配置要点

```yaml
# config/loki-config.yml 核心参数

server:
  http_listen_port: 3100          # HTTP查询API端口
  grpc_listen_port: 9096          # gRPC推送端口

schema_config:
  configs:
    - store: boltdb-shipper       # 使用 boltdb-shipper 索引引擎
      object_store: filesystem    # 文件系统作为对象存储
      schema: v11                 # Schema版本

limits_config:
  retention_period: 720h          # 日志保留30天（720小时）

compactor:
  retention_enabled: true         # 启用基于保留策略的自动清理
  retention_delete_delay: 2h      # 标记后2小时执行物理删除

storage_config:
  boltdb_shipper:
    active_index_directory: /loki/boltdb-shipper-active  # 活跃索引
    cache_location: /loki/boltdb-shipper-cache           # 索引缓存
```

**设计要点**：
- 单节点部署，使用 `inmemory` KV存储环，无需外部 Consul/Etcd
- `max_look_back_period: 0s` 表示查询回溯时间由 `retention_period` 约束（30天）
- 查询缓存与块缓存各 500MB，提升重复查询性能
- gRPC 消息大小上限设为 16MB，支持批量日志推送

### 2.3 Promtail 配置要点

```yaml
# config/promtail-config.yml 核心参数

server:
  http_listen_port: 9080          # 健康检查与热重载端口

clients:
  - url: http://loki:3100/loki/api/v1/push  # Loki推送地址

scrape_configs:
  # 采集任务1: Docker容器日志（通过docker_sd自动发现）
  - job_name: docker-containers
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
    relabel_configs:
      - source_labels: ['container']
        regex: 'delta-.*'         # 只采集delta项目容器
        action: keep

  # 采集任务2: Backend JSON结构化日志
  - job_name: backend-json-logs
    static_configs:
      - __path__: /app/logs/delta-ai.json
    pipeline_stages:
      - json:                     # 解析JSON字段
          expressions:
            timestamp: timestamp
            level: level
            message: message
            stack_trace: stack_trace
      - labels:                   # 提取级别为标签
          level: ""
      - timestamp:                # 使用日志自带时间戳
          source: timestamp
          format: RFC3339
          location: Asia/Shanghai

  # 采集任务3: Nginx访问日志（正则解析combined格式）
  - job_name: nginx-access-logs

  # 采集任务4: Nginx错误日志
  - job_name: nginx-error-logs
```

**采集覆盖范围**：

| 采集目标 | 来源 | 采集方式 | 标签 |
|---------|------|---------|------|
| Backend 结构化日志 | `/app/logs/delta-ai.json` | 静态文件 + JSON解析 | `service=backend`, `log_type=structured` |
| Docker 容器日志 | Docker Socket | 动态发现 + 容器名过滤 | `container=delta-xxx`, `source=docker` |
| Nginx 访问日志 | `/var/log/nginx/access.log` | 静态文件 + 正则解析 | `service=nginx`, `log_type=access` |
| Nginx 错误日志 | `/var/log/nginx/error.log` | 静态文件 + 正则解析 | `service=nginx`, `log_type=error` |

---

## 三、Docker Compose 新增服务

### Loki 服务

| 参数 | 值 | 说明 |
|------|-----|------|
| 镜像 | `grafana/loki:2.9.10` | 兼容 boltdb-shipper 稳定版本 |
| 容器名 | `delta-loki` | |
| 端口映射 | `3100:3100` | HTTP查询API |
| 配置文件 | `./config/loki-config.yml` (只读挂载) | 挂载到 `/etc/loki/` |
| 数据卷 | `loki_data:/loki` | 索引 + 数据块持久化 |
| 健康检查 | `GET /ready` | 15s间隔，5次重试 |
| 启动参数 | `-config.file` + `-target=all` | 单节点运行全部组件 |

### Promtail 服务

| 参数 | 值 | 说明 |
|------|-----|------|
| 镜像 | `grafana/promtail:2.9.10` | 与 Loki 版本一致 |
| 容器名 | `delta-promtail` | |
| 端口映射 | `9080:9080` | 健康检查与热重载 |
| 配置文件 | `./config/promtail-config.yml` (只读挂载) | 挂载到 `/etc/promtail/` |
| 依赖服务 | loki (等待健康检查通过) | |
| Docker Socket | `/var/run/docker.sock:ro` | 容器自动发现 |
| 容器日志目录 | `/var/lib/docker/containers:ro` | Docker日志文件读取 |
| Backend日志 | `backend_logs:/app/logs:ro` | JSON日志文件读取 |
| Nginx日志 | `nginx_logs:/var/log/nginx:ro` | Nginx日志文件读取 |
| 位置记录 | `promtail_positions:/tmp` | 断点续传位置文件 |

### 新增数据卷

| 卷名 | 挂载路径 | 用途 |
|------|---------|------|
| `loki_data` | `/loki` | Loki 索引、数据块、压缩器工作目录 |
| `promtail_positions` | `/tmp` | Promtail 采集位置记录文件 (positions.yaml) |

---

## 四、部署步骤

### 4.1 前置条件

- Docker Engine 20.10+
- Docker Compose v2.x
- 已部署的基础服务：MySQL、Redis、Backend、Nginx
- Backend 已配置 JSON 格式日志输出（logback-spring.xml ✅）

### 4.2 首次部署

```bash
# 1. 进入项目目录
cd d:/Project/AI-SERVERS

# 2. 拉取 Loki + Promtail 镜像
docker pull grafana/loki:2.9.10
docker pull grafana/promtail:2.9.10

# 3. 启动 Loki + Promtail（不影响现有服务）
docker compose up -d loki promtail

# 4. 重启 Grafana 以加载 Loki 数据源配置
docker compose restart grafana

# 5. 验证所有服务状态
docker compose ps
```

### 4.3 一键部署全部服务

```bash
# 启动所有服务（包含新增的 Loki + Promtail）
docker compose up -d

# 预期输出（9个服务全部 healthy/up）：
#   NAME               STATUS
#   delta-mysql        healthy
#   delta-redis        healthy
#   delta-backend      running
#   delta-prometheus   running
#   delta-loki         healthy
#   delta-promtail     running
#   delta-grafana      running
#   delta-nginx        running
```

### 4.4 仅启动日志服务

```bash
# 如果其他服务已在运行，仅启动日志收集栈
docker compose up -d loki promtail
docker compose restart grafana
```

---

## 五、验证方法

### 5.1 Loki 服务验证

```bash
# 1. 检查 Loki 就绪状态
curl http://localhost:3100/ready

# 预期输出: Ready

# 2. 检查 Loki 指标
curl http://localhost:3100/metrics | grep loki_ingester_chunks_created_total

# 3. 查看 Loki 日志
docker compose logs loki --tail 20
```

**关键检查点**：
- [ ] `curl http://localhost:3100/ready` 返回 `Ready`
- [ ] `docker compose ps` 中 `delta-loki` 状态为 `healthy`
- [ ] Loki 日志中无 `level=error` 错误

### 5.2 Promtail 服务验证

```bash
# 1. 检查 Promtail Target 状态
curl http://localhost:9080/targets | python -m json.tool

# 预期: 4个采集任务全部 Ready

# 2. 查看 Promtail 日志
docker compose logs promtail --tail 30

# 预期看到: "successfully connected to loki" 等正常日志
```

**关键检查点**：
- [ ] `http://localhost:9080/targets` 中4个采集任务状态为 **Ready**
- [ ] `docker-jobs` 采集到 `delta-backend`, `delta-mysql`, `delta-redis` 等容器
- [ ] `backend-json-logs` 采集任务无 "file does not exist" 错误
- [ ] Promtail 日志中无 `level=error` 错误

### 5.3 Grafana 日志查询验证

```bash
# 1. 浏览器打开 Grafana: http://localhost:3000
# 2. 使用管理员账号登录: admin / admin123
# 3. 左侧菜单 → "Explore" → 数据源选择 "Loki"
# 4. 在查询框中输入以下 LogQL 测试语句：
```

**LogQL 测试语句**：

```logql
# 查看所有日志（最近5分钟）
{job=~".+"}

# 查看 Backend ERROR 级别日志
{service="backend"} |= "ERROR"

# 查看 Nginx 5xx 错误
{service="nginx", log_type="access"} |~ " 5[0-9]{2} "

# 查看 Backend JSON 日志中某个 Logger 的日志
{logger_name="com.delta.common.service.ai"}

# 查看容器启动日志
{container="delta-backend"} |= "Started DeltaAiApplication"

# 统计最近1小时各级别日志数量
sum by (level) (count_over_time({service="backend"}[1h]))

# 搜索包含特定关键字的日志
{service="backend"} |~ "(?i)(exception|error|timeout)"
```

**关键检查点**：
- [ ] Grafana → **Connections → Data sources** → `Loki` 状态为绿色 ✅
- [ ] Explore 页面选择 Loki 数据源，输入 `{service="backend"}` 有日志返回
- [ ] Backend JSON 日志包含 `level`, `logger_name`, `message` 等结构化标签
- [ ] Nginx 访问日志包含 `status`, `request_method` 等解析后的标签
- [ ] Docker 容器日志包含 `container`, `container_id` 等标签

### 5.4 日志保留验证

```bash
# 1. 检查 Loki 数据目录大小
docker compose exec loki du -sh /loki

# 2. 检查索引文件
docker compose exec loki ls -la /loki/boltdb-shipper-active/

# 3. 检查压缩器状态
docker compose exec loki cat /loki/compactor/compactor.json 2>/dev/null || echo "压缩器尚未执行首次压缩"
```

---

## 六、常见问题排查

| 现象 | 原因 | 解决方案 |
|------|------|---------|
| Loki 无法启动 | 配置文件语法错误 | `docker compose logs loki` 查看错误详情；验证 YAML 缩进 |
| Promtail 无法连接 Loki | Loki 未就绪或网络不通 | 等待 Loki 健康检查通过；确认网络为 `delta-network` |
| Promtail "file does not exist" | Backend 未启动生成日志文件 | 启动 Backend 服务，确保 `/app/logs/delta-ai.json` 存在 |
| Docker 日志采集为空 | Docker Socket 权限不足 | 确认 `/var/run/docker.sock` 已挂载且容器有读取权限 |
| Grafana 无法查询 Loki | 数据源未正确加载 | 检查 Grafana `Connections → Data sources`；确认 UID 为 `loki-delta` |
| 日志时间戳不匹配 | 时区设置不一致 | 确认 `promtail-config.yml` 中 `location: Asia/Shanghai` |
| 磁盘空间增长过快 | 日志量超出预期 | 调整 `retention_period` 减少保留天数；检查是否有日志爆炸 |
| JSON 日志字段未解析 | `pipeline_stages` JSON 路径不匹配 | 对比 `logback-spring.xml` 中 JSON FieldName 与 `promtail-config.yml` 中 `expressions` 一致性 |

---

## 七、后续扩展建议

1. **Grafana 日志仪表盘**：在 Grafana 中创建日志分析仪表盘，展示 ERROR 趋势、Top N 错误、日志吞吐量等
2. **告警规则**：基于 LogQL 配置告警，如「5分钟内ERROR超过10条」的实时告警
3. **日志链路追踪**：在 Backend 中集成 TraceID，通过 `derivedFields` 实现日志→Trace 跳转
4. **日志分级存储**：将 ERROR 日志保留更长时间、INFO 日志按30天轮转
5. **Nginx 代理 Loki**：通过 Nginx 反向代理 Loki API，增加访问控制和 HTTPS
6. **远程存储**：将 Loki 数据迁移到 S3/MinIO 对象存储，实现存储与计算分离
7. **多环境标识**：通过 `external_labels` 区分 `production` / `staging` / `dev` 环境日志

---

## 八、文件变更汇总

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新增 | `config/loki-config.yml` | Loki 服务配置（3100端口、30天保留、boltdb-shipper） |
| 新增 | `config/promtail-config.yml` | Promtail 采集配置（4个采集任务） |
| 修改 | `docker-compose.yml` | 添加 loki + promtail 服务、新增 loki_data + promtail_positions 卷 |
| 修改 | `config/grafana-datasources.yml` | 启用 Loki 数据源 (UID: loki-delta) |
| 保留 | `delta-admin/src/main/resources/logback-spring.xml` | JSON日志格式无需修改（兼容Loki） |