# 变更日志 (CHANGELOG)

本文档记录 Delta AI Customer Service 项目的所有重要变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/),
版本遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [1.0.0] - 2026-05-09

### 新增 (Added)
- 客户唤醒定时任务（CustomerWakeupTask），基于Redis冷却追踪
- 订单-工单自动联动，创建订单时自动生成关联工单
- 订单超时自动取消定时任务（OrderTimeoutTask）
- 工单SLA超时升级增强，Redis违规追踪
- 支付回调接口（PaymentCallbackController），支持微信/支付宝
- 关键词匹配优化（Trie树 O(n)），重构6个意图检测方法
- 数据库14条性能索引优化
- 消息归档策略（MessageArchiveTask），每日凌晨3点自动归档
- 排班日历视图（CompanionScheduleCalendar.vue），Vue3月历网格
- TOTP双因素认证（2FA），RFC 6238标准，纯JDK实现
- 客户生命周期与RFM模型融合（totalOrders、rfmTotalScore维度）
- CsUserCustomer规范数据源统一（syncCustomerAssignments）
- 俱乐部配置操作审计日志
- 生命周期阈值@Value可配置化
- 密码策略升级为字母+数字+特殊字符三要素

### 优化 (Changed)
- 后端：引入Flyway数据库版本管理
- 后端：引入Redis Stream消息队列替代部分定时任务
- 后端：抽象AI模型服务接口（AiModelService），支持多模型适配
- 后端：集成Prometheus + Micrometer性能监控端点
- 后端：日志体系升级（Loki兼容JSON格式 + 异步日志 + 错误分离）
- 后端：API接口版本管理策略（/api/v1/、/api/v2/路径版本化）
- 后端：WebSocket客户端实时聊天能力扩展
- 部署：Docker Compose增加Nginx服务，前后端统一部署
- 部署：GitHub Actions CI/CD流水线（编译+测试+镜像构建+推送）
- 项目：文档目录标准化（docs/reports/、docs/design/、docs/api/、docs/deploy/、docs/database/、docs/changelog/）
- 项目：非核心文件清理归档（_archive/）
- 安全：TwoFactorController添加@PreAuthorize安全注解

### 修复 (Fixed)
- AuthServiceImpl密码校验正则与RegisterDTO同步升级为三要素
- TwoFactorController redisService.get()类型转换错误
- ClubConfigServiceImpl编译错误（LocalDateTime导入、getClubDescription不存在、log冲突）
- MessageArchiveTask未使用导入和字段清理
- TotpUtils未使用Base64导入清理
- WeWorkApiServiceImpl Null安全警告（@SuppressWarnings）
- AuthServiceImpl login方法Null安全警告
- 后端启动失败：Flyway重复迁移脚本冲突（删除delta-common中重复的V1.0__init_schema.sql）
- 后端启动失败：缺少RestTemplate Bean导致DeepSeekAiService注入失败（新增RestTemplateConfig.java）
- 后端Runtime错误：customer_warning_rule表缺失（补充DDL到迁移脚本）
- application-docker.yml中JWT属性名与JwtConfig配置类不对齐（3个Warning消除）
- .gitignore解除*.md和*.sql全局忽略（改为仅忽略临时文件）

### E2E测试补充 (2026-05-09 会话)
- 新增13个页面E2E测试，覆盖率从64%（23/36）提升至100%（36/36）：
  csUserCustomer, companionScheduleCalendar, chatTest, customerProfiles,
  companionLevels, trainings, subscriptions, referrals, qualityChecks,
  pricingPlans, campaigns, serviceTracks, clubConfig

### 数据库变更
- V1.1: messages表按月RANGE分区（11个分区，已存在）
- V1.2: 11条性能优化索引（覆盖orders/customer_profile/companion_schedules/work_orders/cs_user_customer）
- V1.2.1: orders表新增transaction_id(varchar128) + payment_time(datetime) + 索引
- V1.3: 新建messages_archive归档表（19字段+4索引）+ messages表created_at索引
- V1.4: sys_user表新增two_factor_enabled(tinyint) + two_factor_secret(varchar64)

### 文档
- 项目整体分析报告（Delta_AI_项目分析报告.docx）
- 项目质量审查报告（Delta_AI_Customer_Service_项目质量审查报告_20260509.md）
- 修复方案文档（Delta_AI_Customer_Service_修复方案_v1.0.0.md）
- 测试报告（Delta_AI_Customer_Service_测试报告_v1.0.0.md）
- 全模块代码审查报告（Delta_AI_CS_全模块代码审查报告_20260508.md）
- 微服务演进架构规划（microservice_evolution.md）

---

## [0.9.0] - 2026-05-08

### 新增 (Added)
- 基础项目骨架搭建（Spring Boot 3.5.14 + Vue 3.4）
- 用户认证模块（JWT双Token + RBAC权限 + BCrypt加密）
- 客户管理模块（画像、生命周期、RFM模型、满意度）
- 陪玩师管理模块（等级体系、排班）
- 订单与服务模块（订单状态机、服务追踪）
- 工单系统（6状态工作流 + SLA管理）
- AI消息处理（DeepSeek集成 + 关键词匹配 + 自动回复）
- 俱乐部运营模块（配置、订阅、营销）
- 平台接入模块（微信公众号、企业微信）
- 安全防护体系（XSS过滤、登录限流、审计日志、ID混淆、敏感词过滤、数据脱敏、Token黑名单）
- 多环境配置（dev/prod/h2/test）
- Docker多阶段构建 + 容器化部署
- 35个后端测试类 + 5个前端测试

### 数据库
- 初始42张业务表 + 184个索引
- UTF-8编码，逻辑删除策略
- Delta_AI_CS_CREATE_SQL.sql 初始化脚本