# Delta AI Customer Service

三角洲行动陪玩俱乐部 AI 智能客服系统 — 基于 Spring Boot 3.5 + Vue 3 的全栈智能客服解决方案，集成 DeepSeek AI、多平台消息接入、工单管理、客户画像等核心功能。

## 项目概述

本系统为陪玩俱乐部提供一站式 AI 客服解决方案，支持微信公众号、企业微信等多平台消息接入，实现智能问答、关键词匹配、情绪识别、自动转人工等核心客服流程。同时提供客户画像分析、工单管理、排班管理等运营支撑功能。

### 核心能力

- **AI 智能回复**：集成 DeepSeek 大模型，支持多轮对话、上下文理解、个性化人设
- **关键词匹配**：基于关键词库的精准回复，支持精度/模糊匹配和优先级排序
- **情绪识别与转人工**：自动检测负面情绪、下单意图、人工请求，智能触发转人工流程
- **多平台接入**：微信公众号、企业微信（WeCom）、KOOK、YY、测试平台
- **客户画像**：RFM 分层、生命周期阶段、消费趋势、会员等级、风险等级
- **工单系统**：6 状态工作流（待处理→已分配→处理中→已解决→已关闭 + 取消），支持服务追踪
- **运营管理**：陪玩师管理、排班管理、套餐管理、数据统计、满意度评价

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 (Virtual Threads) | 开发语言 |
| Spring Boot | 3.5.14 | 应用框架 |
| MyBatis-Plus | 3.5.16 | ORM框架 |
| MySQL | 9.6.0-commercial | 关系数据库 |
| Redis | 7.x | 缓存/分布式锁 |
| Redisson | 与Spring Boot集成 | 分布式锁实现 |
| Druid | 与Spring Boot集成 | 数据库连接池 |
| DeepSeek API | deepseek-chat | AI大模型 |
| JWT | jjwt 0.12.x | 认证授权 |
| Knife4j | 4.x | API文档 |
| BCrypt | Spring Security | 密码加密 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4 | 前端框架 |
| Vite | 5.0 | 构建工具 |
| Element Plus | 2.5 | UI组件库 |
| Pinia | 2.1 | 状态管理 |
| Axios | 1.6 | HTTP客户端 |

## 项目结构

```
delta-ai-customer-service/
├── delta-common/          # 公共模块：实体、DTO、VO、Service、Mapper、配置、常量、工具
├── delta-admin/           # 管理后台：Controller、SecurityConfig、WebSocket
├── delta-platform/        # 平台适配：微信公众号、企业微信接入
├── delta-message/         # AI 集成：DeepSeek 服务实现
├── delta-ui/              # 前端界面：Vue 3 + Element Plus
├── docs/                  # 设计文档与优化方案
│   └── PRODUCTION_OPTIMIZATION_PLAN.md  # 生产环境优化方案
├── scripts/               # 运维脚本
│   ├── migration_v2_optimized.sql       # 数据库结构迁移脚本（30表/184索引）
│   ├── init_data_v2.sql                 # 核心初始化数据
│   ├── test_data_v1.0.sql               # 测试数据脚本（覆盖24模块/~280条）
│   └── stress_test.ps1                  # 压力测试脚本（80000并发）
├── Dockerfile             # Docker镜像构建
└── docker-compose.yml     # Docker Compose编排
```

### 模块依赖关系

```
delta-admin → delta-common, delta-platform, delta-message
delta-platform → delta-common
delta-message → delta-common
delta-common (基础模块，无内部依赖)
```

## 开发环境快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- MySQL 9.0+ (或 8.0+)
- Redis 7+
- Node.js 18+（前端开发）

### 1. 数据库初始化

```sql
CREATE DATABASE delta_ai_customer_service
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

执行数据库迁移脚本和初始化数据：

```bash
# 结构迁移（30张表，184个索引）
mysql -uroot -p delta_ai_customer_service < scripts/migration_v2_optimized.sql

# 核心初始化数据
mysql -uroot -p delta_ai_customer_service < scripts/init_data_v2.sql

# 测试数据导入（可选，用于手动测试）
mysql -uroot -p delta_ai_customer_service < scripts/test_data_v1.0.sql
```

### 2. 后端配置

编辑 `delta-admin/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/delta_ai_customer_service?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password

deepseek:
  api-key: your_deepseek_api_key
  enabled: true

# 平台接入（根据实际需要启用）
wework:
  enabled: false
```

### 3. 后端启动

```bash
# 编译
mvn clean compile -DskipTests

# 运行（开发环境）
cd delta-admin
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端服务默认启动在 `http://localhost:8080`

### 4. 前端启动

```bash
cd delta-ui
npm install
npm run dev
```

前端开发服务器默认启动在 `http://localhost:5173`

### 5. 默认管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `admin123` | SYS_ADMIN |

> ⚠️ **安全警告**：生产环境务必修改默认密码！

## 功能模块说明

### AI 客服核心流程

```
用户消息 → 平台适配器（微信/企微/KOOK/YY/Test）
  → BaseMessageProcessService
    → 关键词匹配（KeywordMatcherService）
    → 回复查询（ReplyService）
    → AI 生成（DeepSeekService）
    → 转人工判断（情绪/意图/连续失败）
    → 待处理消息创建（PendingMessageService）
    → 管理员通知（WebSocket）
  → 回复发送至平台
```

### 平台接入

| 平台 | 状态 | 接入方式 |
|------|------|----------|
| 微信公众号 | ✅ 已实现 | HTTP回调 + 验签 |
| 企业微信 | ✅ 已实现 | HTTP回调 + AES加解密 + Token管理 |
| KOOK | 📋 规划中/已部分实现 | WebSocket |
| YY | 📋 规划中/已部分实现 | HTTP回调 |
| Test | ✅ 已实现 | HTTP（测试用） |

### 工单系统

- **6 状态工作流**：待处理 → 已分配 → 处理中 → 已解决 → 已关闭（+ 已取消）
- **4 种工单类型**：COMPLAINT（投诉）、SERVICE（服务）、CONSULTATION（咨询）、APPOINTMENT（预约）
- **3 级优先级**：LOW/NORMAL/HIGH（18h/12h/4h 超时处理）
- **服务追踪**：支持从咨询到完成的完整生命周期追踪
- **权限控制**：系统管理员(ALL)/客服主管(全员)/客服人员(仅自己) 三级数据隔离

### 客户画像

- **RFM 分层**：VIP_HIGH_VALUE / ACTIVE / STABLE / NEW / AT_RISK / LOST
- **生命周期**：新客 → 活跃 → 稳定 → 沉默 → 流失
- **消费趋势**：增长 ↑ / 稳定 → / 下降 ↓
- **会员等级**：REGULAR / SILVER / GOLD / DIAMOND
- **风险等级**：LOW / MEDIUM / HIGH

## API 文档

启动后端服务后，访问 Knife4j 文档页面：

```
http://localhost:8080/doc.html
```

### 主要 API 端点

| 路径 | 说明 |
|------|------|
| `/api/v1/auth/**` | 认证授权（登录/登出/刷新-Token） |
| `/api/v1/customers/**` | 客户管理 |
| `/api/v1/customer-profiles/**` | 客户画像 |
| `/api/v1/customer-satisfaction/**` | 满意度评价 |
| `/api/v1/messages/**` | 消息管理 |
| `/api/v1/pending-messages/**` | 待处理消息 |
| `/api/v1/conversation-sessions/**` | 会话管理 |
| `/api/v1/companions/**` | 陪玩师管理 |
| `/api/v1/companion-levels/**` | 陪玩师等级 |
| `/api/v1/companion-schedules/**` | 排班管理 |
| `/api/v1/orders/**` | 订单管理 |
| `/api/v1/customer-order-records/**` | 订单操作记录 |
| `/api/v1/work-orders/**` | 工单管理 |
| `/api/v1/work-order-records/**` | 工单操作记录 |
| `/api/v1/service-tracks/**` | 服务追踪 |
| `/api/v1/service-items/**` | 服务项目管理 |
| `/api/v1/activity-packages/**` | 活动套餐管理 |
| `/api/v1/game-configs/**` | 游戏配置 |
| `/api/v1/faq-items/**` | FAQ 知识库 |
| `/api/v1/game-knowledge/**` | 游戏知识库 |
| `/api/v1/keywords/**` | 关键词管理 |
| `/api/v1/replies/**` | 自动回复模板 |
| `/api/v1/sys-users/**` | 系统用户管理 |
| `/api/v1/cs-user-customers/**` | 客服-客户分配 |
| `/api/v1/club-config/**` | 俱乐部配置 |
| `/api/v1/ai-config/**` | AI 模型配置 |
| `/api/v1/platform-configs/**` | 平台接入配置 |
| `/api/v1/stats/**` | 数据统计 |
| `/api/v1/operation-logs/**` | 操作日志 |
| `/wework/callback` | 企业微信回调 |
| `/wechat` | 微信公众号回调 |

## 数据库

### 表结构概览

- **30 张业务表**，严格遵循 3NF 设计
- **184 个索引**，含 FULLTEXT 全文索引（游戏知识库/FAQ）
- **3 条逻辑外键**，应用层约束保证数据一致性
- InnoDB 引擎，utf8mb4 字符集，utf8mb4_unicode_ci 排序规则

### 核心表分类

| 分类 | 表名 | 说明 |
|------|------|------|
| 用户体系 | `sys_user`, `users`, `cs_user_customer` | 管理员/客户/分配关系 |
| 陪玩师 | `companions`, `companion_levels`, `companion_schedules` | 陪玩师信息/等级/排班 |
| 订单 | `orders`, `customer_order_record` | 订单流水/操作记录 |
| 工单 | `work_orders`, `work_order_records`, `work_order_attachments` | 工单流转/附件 |
| 消息 | `messages`, `pending_messages`, `conversation_sessions` | 消息/待处理/会话 |
| 客户画像 | `customer_profile`, `customer_satisfaction` | RFM画像/满意度 |
| 配置 | `club_config`, `game_config`, `ai_config`, `platform_configs` | 系统配置 |
| 知识库 | `faq_items`, `game_knowledge`, `keywords`, `replies` | FAQ/游戏知识/关键词 |
| 服务 | `service_item`, `service_price_rule`, `service_tracks` | 服务项目/定价/追踪 |
| 运营 | `activity_package`, `operation_logs` | 套餐/审计日志 |

## 生产环境注意事项

1. **安全配置**：生产环境务必修改 JWT 密钥（`jwt.secret`）、数据库密码（`spring.datasource.password`）、Redis 密码等
2. **DeepSeek API**：需配置有效的 API Key（`deepseek.api-key`），通过环境变量 `DEEPSEEK_API_KEY` 注入
3. **企业微信**：需在管理后台配置回调URL和可信域名，涉及 `token`/`encodingAesKey`/`corpId` 等配置
4. **Docker 部署**：使用 `eclipse-temurin:21-jre` 镜像，已配置优雅关闭
5. **数据隔离**：客服人员只能查看自己负责的客户数据，由 Service 层 `applyDataScope` 统一控制
6. **CORS 安全**：生产环境务必配置具体的 `allowedOrigins`，不能使用 `*`
7. **日志级别**：生产环境 `logging.level.com.delta=INFO`
8. **Druid 监控**：默认仅允许 `127.0.0.1` 访问，可通过 `allow` 配置项调整

## 测试数据

项目提供了完整的测试数据脚本 `scripts/test_data_v1.0.sql`：

- **覆盖模块**：24/24（全量覆盖）
- **数据记录**：约 280 条
- **业务场景**：客户咨询→AI回复→转人工→下单→工单→满意度评价（完整闭环）
- **支持幂等**：可重复执行，自动清理后重新导入

### 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `admin123` | 系统管理员 |
| `zhang_leader` | `cs123456` | 客服主管 |
| `li_staff` | `cs123456` | 客服人员 |

## 开发规范

本项目遵循阿里巴巴 Java 开发规范（P3C标准）。

核心规范要点：

- 常量使用 `final class` + `private` 构造器，禁止使用接口定义常量
- 禁止魔法字符串/数字，统一使用常量类
- 业务异常使用 `BusinessException`，禁止 `RuntimeException`
- Controller 不包含业务逻辑，仅做参数提取和委托
- 方法长度不超过 80 行，参数不超过 5 个
- MyBatis-Plus 使用 `LambdaQueryWrapper`，禁止字符串列名
- SQL 查询使用参数化占位符 `#{}`，禁止 `${}` 拼接
- 所有文件使用 UTF-8 编码，代码保持中文注释

## 优化状态

生产环境优化已完成，详见：[PRODUCTION_OPTIMIZATION_PLAN.md](docs/PRODUCTION_OPTIMIZATION_PLAN.md)

| 阶段 | 状态 | 进度 |
|------|------|------|
| 安全加固 | ✅ 完成 | 6/6 |
| 配置部署优化 | ✅ 完成 | 6/6 |
| 代码质量优化 | ✅ 完成 | 5/5 |
| 性能优化 | ✅ 完成 | 3/3 |
| 前端优化 | ✅ 完成 | 3/3 |
| 核心Bug修复 | ✅ 完成 | 2/2 |
| **总计** | **✅ 全部完成** | **23/23** |

> 全量编译：**零错误、零警告** ✅

## 作者

刘建国 (Liu Jianguo) - 2026

## License

Private - All Rights Reserved
