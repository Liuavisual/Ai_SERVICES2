# Delta AI Customer Service

三角洲行动陪玩俱乐部 AI 智能客服系统 — 基于 Spring Boot 3.5 + Vue 3 的全栈智能客服解决方案，集成 DeepSeek AI、多平台消息接入、工单管理、客户画像等核心功能。

## 项目概述

本系统为陪玩俱乐部提供一站式 AI 客服解决方案，支持微信公众号、企业微信等多平台消息接入，实现智能问答、关键词匹配、情绪识别、自动转人工等核心客服流程。同时提供客户画像分析、工单管理、排班管理等运营支撑功能。

### 核心能力

- **AI 智能回复**：集成 DeepSeek 大模型，支持多轮对话、上下文理解、个性化人设
- **关键词匹配**：基于关键词库的精准回复，支持优先级排序
- **情绪识别与转人工**：自动检测负面情绪、下单意图、人工请求，智能触发转人工流程
- **多平台接入**：微信公众号、企业微信（WeCom），可扩展 KOOK/YY 等平台
- **客户画像**：RFM 分层、生命周期阶段、消费趋势、会员等级、风险等级
- **工单系统**：6 状态工作流（新建→处理中→待确认→已完成→已关闭+已取消），支持服务追踪
- **运营管理**：排班管理、伴玩管理、套餐管理、数据统计

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 开发语言 |
| Spring Boot | 3.5.14 | 应用框架 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 关系数据库 |
| Redis | 7 | 缓存/分布式锁 |
| Redisson | - | 分布式锁实现 |
| DeepSeek API | - | AI 大模型 |
| JWT | - | 认证授权 |
| Swagger/OpenAPI | 3 | API 文档 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4 | 前端框架 |
| Vite | 5.0 | 构建工具 |
| Element Plus | 2.5 | UI 组件库 |
| Pinia | 2.1 | 状态管理 |
| Axios | 1.6 | HTTP 客户端 |

## 项目结构

```
delta-ai-customer-service/
├── delta-common/          # 公共模块：实体、DTO、VO、Service、Mapper、常量、工具
├── delta-admin/           # 管理后台：Controller、SecurityConfig、WebSocket
├── delta-platform/        # 平台适配：微信公众号、企业微信接入
├── delta-message/         # AI 集成：DeepSeek 服务实现
├── delta-ui/              # 前端界面：Vue 3 + Element Plus
├── docs/                  # 设计文档
├── scripts/               # 运维脚本
├── Dockerfile             # Docker 镜像构建
└── docker-compose.yml     # Docker Compose 编排
```

### 模块依赖关系

```
delta-admin → delta-common, delta-platform, delta-message
delta-platform → delta-common
delta-message → delta-common
delta-common (基础模块，无内部依赖)
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- MySQL 8.0+
- Redis 7+
- Node.js 18+（前端开发）

### 1. 数据库初始化

创建 MySQL 数据库：

```sql
CREATE DATABASE delta_ai_customer_service
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 2. 后端配置

编辑 `delta-admin/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/delta_ai_customer_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379

deepseek:
  api-key: your_deepseek_api_key
  enabled: true

wework:
  enabled: false
  corp-id: your_corp_id
  agent-id: 1000002
  app-secret: your_app_secret
  contact-secret: your_contact_secret
  callback-token: your_callback_token
  callback-encoding-aes-key: your_encoding_aes_key

wx:
  mp:
    enabled: false
    token: your_wechat_token
```

### 3. 后端启动

```bash
# 编译
mvn clean compile

# 运行
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

### 5. Docker 部署

```bash
# 使用 Docker Compose 一键部署
docker-compose up -d

# 自定义环境变量
DEEPSEEK_API_KEY=your_key MYSQL_ROOT_PASSWORD=your_pwd docker-compose up -d
```

## 功能模块说明

### AI 客服核心流程

```
用户消息 → 平台适配器（微信/企微）
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
| 微信公众号 | 已实现 | HTTP 回调 + 验签 |
| 企业微信 | 已实现 | HTTP 回调 + AES 加解密 + Token 管理 |
| KOOK | 规划中 | WebSocket |
| YY | 规划中 | HTTP 回调 |

### 工单系统

- **6 状态工作流**：新建 → 处理中 → 待确认 → 已完成 → 已关闭（+ 已取消）
- **3 级优先级**：普通/紧急/特急，不同超时时间
- **服务追踪**：咨询中 → 已预约 → 服务中 → 服务完成 → 已确认
- **权限控制**：系统管理员/客服主管/客服人员三级角色数据隔离

### 客户画像

- **RFM 分层**：高价值/中价值/低价值/流失风险/新客户
- **生命周期**：新客/活跃/稳定/沉默/流失
- **消费趋势**：增长/稳定/下降
- **会员等级**：普通/银卡/金卡/钻石
- **风险等级**：低/中/高

## API 文档

启动后端服务后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

### 主要 API 端点

| 路径 | 说明 |
|------|------|
| `/api/auth/**` | 认证授权 |
| `/api/customers/**` | 客户管理 |
| `/api/messages/**` | 消息管理 |
| `/api/pending-messages/**` | 待处理消息 |
| `/api/work-orders/**` | 工单管理 |
| `/api/stats/**` | 数据统计 |
| `/api/wework/callback` | 企业微信回调 |
| `/wechat` | 微信公众号回调 |

## 注意事项

1. **安全配置**：生产环境务必修改 JWT 密钥、数据库密码、Redis 密码等敏感配置
2. **DeepSeek API**：AI 功能依赖 DeepSeek API，需配置有效的 API Key
3. **企业微信**：需在企业微信管理后台配置回调 URL 和可信域名
4. **Token 管理**：企业微信 access_token 通过 Redis 缓存 + Redisson 分布式锁管理，避免并发刷新
5. **数据隔离**：客服人员只能查看自己负责的客户数据，由 Service 层 `applyDataScope` 统一控制
6. **导出限制**：数据导出使用分页查询（每页 10000 条），避免内存溢出
7. **Docker 部署**：Dockerfile 中 Java 版本为 17，需与实际 JDK 版本保持一致

## 开发规范

本项目遵循阿里巴巴 Java 开发规范，详见 `.trae/skills/alibaba-dev-standards/SKILL.md`。

核心规范要点：

- 常量使用 `final class` + `private` 构造器，禁止使用接口定义常量
- 禁止魔法字符串/数字，统一使用常量类
- 业务异常使用 `BusinessException`，禁止 `RuntimeException`
- Controller 不包含业务逻辑，仅做参数提取和委托
- 方法长度不超过 80 行，参数不超过 5 个
- MyBatis-Plus 使用 `LambdaQueryWrapper`，禁止字符串列名
- SQL 查询使用参数化占位符，禁止字符串拼接

## License

Private - All Rights Reserved
