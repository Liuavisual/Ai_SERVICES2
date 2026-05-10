# Delta AI Customer Service 微服务演进规划

> 作者：刘建国 | 版本：1.0 | 日期：2026-05-09

---

## 一、现状分析

当前项目为单体 Maven 多模块架构：

- **delta-common**: 公共基础模块（实体、Mapper、Service、工具类、配置）
- **delta-admin**: 管理后台模块（Controller、Security、WebSocket）
- **delta-message**: AI消息处理模块（DeepSeek集成、关键词匹配）
- **delta-platform**: 平台接入模块（微信、企业微信、支付回调）
- **delta-ui**: 前端模块（Vue 3 + Element Plus）

## 二、演进目标

### 2.1 驱动因素

- delta-common 模块承载过多职责（~47个实体类、42个Mapper、70+个Service）
- AI消息处理是CPU密集型任务，需要独立扩缩容
- 平台接入层需要更高的可用性（与第三方API交互频繁）
- 工单系统和订单系统有独立的事务边界

### 2.2 目标架构

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  API Gateway │  │ Auth Service │  │ Order Service│  │ WorkOrder   │
│   (Nginx/   │  │ (用户认证)    │  │ (订单管理)    │  │ Service     │
│   Kong)     │  │              │  │              │  │ (工单系统)   │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │                │
       └────────────────┴────────────────┴────────────────┘
                                │
                  ┌─────────────┼─────────────┐
                  │             │             │
           ┌──────┴──────┐ ┌───┴────┐ ┌──────┴──────┐
           │ AI Message   │ │Customer│ │ Platform    │
           │ Service      │ │Service │ │ Gateway     │
           │ (DeepSeek)   │ │(画像)   │ │ (微信/企微) │
           └─────────────┘ └────────┘ └─────────────┘
```

## 三、演进路线

### 第一阶段：服务化准备（6个月内）

| 步骤 | 内容 | 产出 |
|------|------|------|
| 1. 接口抽象 | 为每个业务域定义独立的Service Facade接口 | 接口层代码 |
| 2. 数据库拆分 | 划分子域边界，确定每张表归属哪个服务 | 数据字典 |
| 3. 消息通信 | 使用Redis Stream替代直接Service调用 | 消息队列 |
| 4. 配置中心 | 引入Nacos/Apollo管理分布式配置 | 配置管理 |

### 第二阶段：核心服务拆分（6-12个月）

| 服务 | 拆分内容 | 通信方式 |
|------|----------|----------|
| Auth Service | 用户认证、JWT、2FA、RBAC权限 | REST + gRPC |
| AI Message Service | DeepSeek调用、关键词匹配、情绪分析 | Redis Stream (异步) |
| Order Service | 订单CRUD、支付、服务追踪 | REST + 本地事务 |
| Customer Service | 客户画像、生命周期、RFM模型 | REST |
| Platform Gateway | 微信/企微适配、支付回调 | REST |

### 第三阶段：平台化（12-24个月）

- 引入 Spring Cloud Gateway 统一入口
- 引入 Sleuth + Zipkin 分布式链路追踪
- 容器化编排（Kubernetes）
- 多租户支持

## 四、关键技术选型

| 组件 | 选型 | 理由 |
|------|------|------|
| 服务框架 | Spring Cloud 2024.x | 与Spring Boot 3.5生态兼容 |
| RPC通信 | gRPC (via Spring gRPC) | 高性能、强类型、支持流式 |
| 异步消息 | Redis Stream (近期) → Kafka (远期) | 渐进式升级 |
| 服务注册 | Nacos (推荐) 或 Consul | 国内生态优势 |
| 配置管理 | Nacos Config | 与注册中心统一 |
| 网关 | Spring Cloud Gateway | 响应式、高性能 |

## 五、风险与缓解

| 风险 | 缓解措施 |
|------|----------|
| 分布式事务一致性 | Saga模式 + 最终一致性 |
| 服务间调用延迟 | gRPC + 本地缓存 + 连接池 |
| 运维复杂度 | 统一Docker Compose → K8s渐进迁移 |
| 数据库拆分困难 | 先逻辑隔离，再物理拆分 |