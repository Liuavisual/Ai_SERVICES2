# Delta Common 模块拆分方案

> 版本: v1.0.0
> 日期: 2026-05-10
> 作者: 刘建国
> 状态: 执行中

---

## 一、拆分背景

`delta-common` 模块当前承载了全部公共代码（约308个Java文件），违反单一职责原则。拆分为三个子模块，实现关注点分离和依赖解耦。

---

## 二、拆分前概览

| 目录 | 文件数 | 说明 |
|------|--------|------|
| constant/ | 11 | 常量定义 |
| enums/ | 11 | 枚举定义 |
| annotation/ | 4 | 自定义注解 |
| serializer/ | 2 | 序列化器 |
| util/ | 7 | 工具类 |
| exception/ | 2 | 异常类 |
| entity/ | 49 | 数据实体DO |
| dto/ | 34 | 数据传输对象 |
| vo/ | 44 | 视图对象 |
| event/ | 1 | Spring事件 |
| mapper/ | 42 | MyBatis-Plus Mapper接口 |
| service/ | 约72 | Service接口+实现（含ai/、impl/、matcher/） |
| aspect/ | 2 | AOP切面 |
| interceptor/ | 1 | 拦截器 |
| config/ | 9 | Spring配置类 |
| task/ | 5 | 定时任务 |
| protection/ | 1 | 服务保护 |
| agent/ | 2 | 测试Agent |
| **合计** | **~308** | |

---

## 三、模块划分方案

### 3.1 delta-common-core（基础核心模块）

**定位**：零外部依赖的基础设施层，仅依赖JDK标准库和纯工具库。

**包含目录**（约32文件）：

| 目录 | 文件数 | 文件列表 |
|------|--------|----------|
| constant/ | 11 | AiCustomerServiceConstants, AiPersonalityConstants, ApiVersionConstants, BusinessStatusConstants, CustomerLifecycleConstants, CustomerProfileConstants, ExportConstants, MessageConstants, PlatformConstants, WeWorkConstants, WorkOrderConstants |
| enums/ | 11 | InterventionTypeEnum, MessageDirectionEnum, PendingMessageStatusEnum, PlatformEnum, ReplyTriggerTypeEnum, RoleEnum, ServiceTrackStatusEnum, UserStatusEnum, WorkOrderPriorityEnum, WorkOrderStatusEnum, WorkOrderTypeEnum |
| annotation/ | 4 | AuditLog, ObfuscatedId, ProtectedLogic, RequirePermission |
| serializer/ | 2 | ObfuscatedIdDeserializer, ObfuscatedIdSerializer |
| util/ | 3 | DesensitizeUtils, IdObfuscateUtils, TotpUtils |
| exception/ | 1 | BusinessException |

**pom.xml依赖**（仅纯工具库）：
- `hutool-all` - 工具库
- `lombok` - 代码简化 (optional)
- `jackson-databind` - JSON序列化（serializer需要）
- `slf4j-api` - 日志门面
- `spring-security-crypto` - BCrypt加密

---

### 3.2 delta-common-entity（实体/数据层模块）

**定位**：定义数据结构和数据访问接口，依赖core模块。

**包含目录**（约168文件）：

| 目录 | 文件数 | 文件列表 |
|------|--------|----------|
| entity/ | 49 | ActivityPackage, ActivityPackageUsage, AiConfig, AiPersonalityConfig, BaseEntity, Campaign, ClubConfig, ClubLevelPrice, ClubSubscription, Companion, CompanionGame, CompanionLevel, CompanionSchedule, CompanionSettlement, CompanionTraining, ConversationSession, CsUserCustomer, CustomerOrderRecord, CustomerProfile, CustomerSatisfaction, CustomerWarningRule, FaqItem, GameConfig, GameKnowledge, Keyword, Message, OperationLog, Order, PendingMessage, PlatformConfig, PricingPlan, QualityCheckRecord, ReferralRecord, Reply, ReplyUsageStat, RevenueDailyReport, ServiceItem, ServicePriceRule, ServiceTrack, SysPermission, SysRole, SysRolePermission, SysUser, SysUserRole, User, WorkOrder, WorkOrderAttachment, WorkOrderRecord, WorkOrderSla |
| dto/ | 34 | ActivityPackageDTO, AiConfigUpdateDTO, AuditUserDTO, ChatTestSendDTO, ClubConfigDTO, ClubLevelPriceDTO, CompanionDTO, CompanionLevelDTO, CompanionScheduleDTO, CsUserCustomerDTO, CustomerOrderRecordDTO, CustomerProfileUpdateDTO, CustomerSatisfactionDTO, FaqItemDTO, GameConfigDTO, ImportResultDTO, KeywordDTO, LoginDTO, OrderCreateDTO, OrderQueryDTO, PendingMessageHandleDTO, PersonalityConfigDTO, PlatformConfigDTO, RefreshTokenDTO, RegisterDTO, ReplyDTO, ServiceItemDTO, ServicePriceRuleDTO, ServiceTrackBookDTO, ServiceTrackEndDTO, SysUserDTO, WeWorkCallbackDTO, WeWorkSendMessageDTO, WorkOrderConfirmDTO, WorkOrderCreateDTO, WorkOrderRecordDTO, WorkOrderSubmitDTO |
| vo/ | 44 | ActivityPackageVO, AiConfigVO, BaseVO, CampaignVO, ChatTestReplyVO, ClubConfigVO, ClubLevelPriceVO, ClubSubscriptionVO, CompanionLevelVO, CompanionScheduleVO, CompanionSettlementVO, CompanionTrainingVO, CompanionVO, CsUserCustomerVO, CustomerOrderRecordVO, CustomerProfileVO, CustomerSatisfactionVO, CustomerVO, FaqItemVO, GameConfigVO, KeywordVO, LoginVO, MessageVO, NotificationVO, OrderVO, PendingMessageVO, PersonalityConfigVO, PlatformConfigVO, PricingPlanVO, QualityCheckRecordVO, ReferralRecordVO, ReplyVO, Result, RevenueDailyReportVO, ServiceItemVO, ServicePriceRuleVO, ServiceTrackVO, StatsVO, SysPermissionVO, SysRoleVO, SysUserVO, WorkOrderAttachmentVO, WorkOrderRecordVO, WorkOrderVO |
| event/ | 1 | PendingMessageCreatedEvent |
| mapper/ | 42 | ActivityPackageMapper, AiConfigMapper, AiPersonalityConfigMapper, CampaignMapper, ClubConfigMapper, ClubLevelPriceMapper, ClubSubscriptionMapper, CompanionLevelMapper, CompanionMapper, CompanionScheduleMapper, CompanionSettlementMapper, CompanionTrainingMapper, ConversationSessionMapper, CsUserCustomerMapper, CustomerOrderRecordMapper, CustomerProfileMapper, CustomerSatisfactionMapper, CustomerWarningRuleMapper, FaqItemMapper, GameConfigMapper, GameKnowledgeMapper, KeywordMapper, MessageMapper, OperationLogMapper, OrderMapper, PendingMessageMapper, PlatformConfigMapper, PricingPlanMapper, QualityCheckRecordMapper, ReferralRecordMapper, ReplyMapper, RevenueDailyReportMapper, ServiceItemMapper, ServicePriceRuleMapper, ServiceTrackMapper, SysPermissionMapper, SysRoleMapper, SysRolePermissionMapper, SysUserMapper, SysUserRoleMapper, UserMapper, WorkOrderAttachmentMapper, WorkOrderMapper, WorkOrderRecordMapper |
| util/ | 1 | VoUtils |

**pom.xml依赖**（数据层）：依赖 `delta-common-core`，额外包含：
- `mybatis-plus-spring-boot3-starter` - ORM框架
- `mybatis-plus-jsqlparser` - 分页插件
- `jakarta.persistence-api` - @Table/@Index注解
- `swagger-annotations` (knife4j) - API文档注解
- `spring-boot-starter-validation` - JSR-380验证
- `jackson-databind` - JSON处理

---

### 3.3 delta-common-service（业务服务模块）

**定位**：提供完整的业务服务能力，依赖entity模块。其他模块（admin/platform/message）只需依赖此模块即可获得全部能力。

**包含目录**（约108文件）：

| 目录 | 文件数 | 文件列表 |
|------|--------|----------|
| config/ | 9 | ApiVersionConfig, FlywayConfig, JwtConfig, MybatisPlusConfig, PrometheusMetricsConfig, RedisConfig, RedissonConfig, RestTemplateConfig, SensitiveWordConfig |
| service/ (接口) | 约40 | ActivityPackageService, AiConfigService, AuthService, ..., WorkOrderService（完整Service接口列表见附录） |
| service/impl/ | 约40 | ActivityPackageServiceImpl, AiConfigServiceImpl, ..., WorkOrderServiceImpl（完整实现列表见附录） |
| service/ai/ | 3 | AiModelHealthIndicator, AiModelService, DeepSeekAiService |
| service/matcher/ | 2 | KeywordMatcherService, KeywordMatcherServiceImpl |
| aspect/ | 2 | AuditLogAspect, ProtectionAspect |
| interceptor/ | 1 | PermissionInterceptor |
| task/ | 5 | CustomerWakeupTask, MessageArchiveTask, OrderTimeoutTask, PendingMessageEscalationTask, WorkOrderEscalationTask |
| protection/ | 1 | ServiceProtectionManager |
| agent/ | 2 | AutoTestRunner, TestAgentOrchestrator |
| util/ | 3 | JwtUtils, RateLimiter, ExcelUtils |
| exception/ | 1 | GlobalExceptionHandler |

**pom.xml依赖**（全部运行时依赖）：依赖 `delta-common-entity`，额外包含：
- `spring-boot-starter-web` - Web框架
- `spring-boot-starter-aop` - AOP支持
- `spring-boot-starter-data-redis` - Redis
- `spring-boot-starter-actuator` - 健康检查
- `redisson-spring-boot-starter` - 分布式锁
- `flyway-core` + `flyway-mysql` - 数据库迁移
- `druid-spring-boot-3-starter` - 连接池
- `mysql-connector-j` - MySQL驱动
- `micrometer-registry-prometheus` - 监控
- `logstash-logback-encoder` - JSON日志
- `poi-ooxml` - Excel处理
- `sensitive-word` - 敏感词库
- `jjwt-api/impl/jackson` - JWT
- `knife4j-openapi3-jakarta-spring-boot-starter` - API文档
- `mapstruct` / `mapstruct-processor` - 对象映射

**resources/** 资源文件：
- `application.yml` - 公共配置
- `META-INF/additional-spring-configuration-metadata.json` - 配置元数据
- `logback-spring.xml` - 日志配置（如有）

---

## 四、模块依赖关系图

```
                    +---------------------+
                    |  delta-common-core  |
                    |  (32 files)         |
                    |  基础工具+常量+枚举  |
                    +----------+----------+
                               |
                               v
                    +---------------------+
                    | delta-common-entity |
                    | (168 files)         |
                    | 实体+DTO+VO+Mapper  |
                    +----------+----------+
                               |
                               v
                    +---------------------+
                    |delta-common-service |
                    | (108 files)         |
                    | 业务逻辑+配置+切面   |
                    +----+--------+-------+
                         |        |        |
              +----------+---+    |    +---+----------+
              | delta-admin  |    |    |delta-platform|
              | (管理后台)   |    |    | (平台接入)   |
              +--------------+    |    +--------------+
                          +-------+--------+
                          | delta-message  |
                          | (消息处理)     |
                          +----------------+
```

**依赖链**：
```
delta-common-core     -- 无内部模块依赖，仅依赖第三方纯工具库
delta-common-entity   -- com.delta:delta-common-core
delta-common-service  -- com.delta:delta-common-entity
delta-admin           -- com.delta:delta-common-service (替换原有的 delta-common)
delta-platform        -- com.delta:delta-common-service (替换原有的 delta-common)
delta-message         -- com.delta:delta-common-service (替换原有的 delta-common)
```

---

## 五、跨模块依赖分析

| 源类 | 目标类 | 从模块 | 到模块 | 合规性 |
|------|--------|--------|--------|--------|
| GlobalExceptionHandler | BusinessException | service | core | OK (单向) |
| GlobalExceptionHandler | Result (VO) | service | entity | OK (单向) |
| PermissionInterceptor | RequirePermission | service | core | OK (单向) |
| AuditLogAspect | AuditLog | service | core | OK (单向) |
| ProtectionAspect | ProtectedLogic | service | core | OK (单向) |
| ServiceImpl类 | Mapper接口 | service | entity | OK (单向) |
| ServiceImpl类 | Entity/DTO/VO | service | entity | OK (单向) |
| RestTemplateConfig | (无内部引用) | service | - | OK |
| MybatisPlusConfig | BaseEntity | service | entity | OK (单向) |
| JwtUtils | JwtConfig | service | service | OK (同模块) |
| VoUtils | VO类 | entity | entity | OK (同模块) |

**结论**：所有跨模块引用都是单向的 core <- entity <- service，无循环依赖。

---

## 六、迁移步骤清单

### 步骤1：准备工作
- [x] 分析现有代码结构
- [x] 分析跨模块依赖关系
- [x] 确认模块划分方案

### 步骤2：创建模块骨架
- [ ] 创建 `delta-common-core/` 目录和 `pom.xml`
- [ ] 创建 `delta-common-entity/` 目录和 `pom.xml`
- [ ] 创建 `delta-common-service/` 目录和 `pom.xml`
- [ ] 创建各模块的 `src/main/java/com/delta/common/` 包结构

### 步骤3：迁移源文件
- [ ] 复制 core 模块文件 (constant/, enums/, annotation/, serializer/, util/, exception/)
- [ ] 复制 entity 模块文件 (entity/, dto/, vo/, event/, mapper/, util/)
- [ ] 复制 service 模块文件 (service/, aspect/, interceptor/, config/, task/, protection/, agent/, util/, exception/)
- [ ] 迁移 resources/ 到 delta-common-service

### 步骤4：更新父POM和依赖
- [ ] 修改根 `pom.xml` modules列表
- [ ] 修改 `delta-admin/pom.xml` 依赖
- [ ] 修改 `delta-platform/pom.xml` 依赖
- [ ] 修改 `delta-message/pom.xml` 依赖

### 步骤5：更新外部引用（如Dockerfile）
- [ ] 更新 `Dockerfile` 中的模块路径
- [ ] 更新 `.dockerignore` 中模块路径

### 步骤6：验证
- [ ] 运行 `mvn clean compile -DskipTests`
- [ ] 修复编译错误
- [ ] 确认所有模块编译通过

---

## 七、风险与注意事项

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Mapper接口跨模块注入失败 | 编译或运行时错误 | 确保Spring扫描路径包含所有模块 |
| 测试文件归属问题 | 测试执行失败 | 测试文件按Service层归属到service模块 |
| Dockerfile路径变更 | 构建失败 | 同步更新Dockerfile中的COPY路径 |
| 循环依赖 | 编译失败 | 严格遵循core->entity->service单向依赖 |

---

## 八、附录：Service层完整文件列表

### Service接口 (service/)
1. ActivityPackageService.java
2. AiConfigService.java
3. AuthService.java
4. CacheService.java
5. CacheStatsService.java
6. CampaignService.java
7. ChatTestService.java
8. ClubConfigService.java
9. ClubSubscriptionService.java
10. CompanionService.java
11. CompanionLevelService.java
12. CompanionScheduleService.java
13. CompanionSettlementService.java
14. CompanionTrainingService.java
15. ContentSafetyService.java
16. CsUserCustomerService.java
17. CustomerService.java
18. CustomerLifecycleService.java
19. CustomerProfileService.java
20. CustomerSatisfactionService.java
21. DeepSeekService.java
22. DistributedLockService.java
23. EmotionIntelligenceService.java
24. FaqItemService.java
25. GameConfigService.java
26. GameKnowledgeService.java
27. KeywordService.java
28. MessageService.java
29. MessageQueueService.java
30. OrderService.java
31. PendingMessageService.java
32. PermissionService.java
33. PersonalityConfigService.java
34. PlatformConfigService.java
35. PricingPlanService.java
36. QualityCheckRecordService.java
37. RedisService.java
38. ReferralRecordService.java
39. ReplyService.java
40. RevenueDailyReportService.java
41. ServiceItemService.java
42. ServiceTrackService.java
43. StatsService.java
44. SysUserService.java
45. WorkOrderService.java

### Service实现 (service/impl/)
1. ActivityPackageServiceImpl.java
2. AiConfigServiceImpl.java
3. AuthServiceImpl.java
4. BaseMessageProcessService.java
5. CacheStatsServiceImpl.java
6. CampaignServiceImpl.java
7. ChatTestServiceImpl.java
8. ClubConfigServiceImpl.java
9. ClubSubscriptionServiceImpl.java
10. CompanionServiceImpl.java
11. CompanionLevelServiceImpl.java
12. CompanionScheduleServiceImpl.java
13. CompanionSettlementServiceImpl.java
14. CompanionTrainingServiceImpl.java
15. ContentSafetyServiceImpl.java
16. CsUserCustomerServiceImpl.java
17. CustomerServiceImpl.java
18. CustomerLifecycleServiceImpl.java
19. CustomerProfileServiceImpl.java
20. CustomerSatisfactionServiceImpl.java
21. DistributedLockServiceImpl.java
22. EmotionIntelligenceServiceImpl.java
23. FaqItemServiceImpl.java
24. GameConfigServiceImpl.java
25. GameKnowledgeServiceImpl.java
26. KeywordServiceImpl.java
27. MessageServiceImpl.java
28. MessageQueueServiceImpl.java
29. OrderServiceImpl.java
30. PendingMessageServiceImpl.java
31. PermissionServiceImpl.java
32. PersonalityConfigServiceImpl.java
33. PlatformConfigServiceImpl.java
34. PricingPlanServiceImpl.java
35. QualityCheckRecordServiceImpl.java
36. RedisServiceImpl.java
37. ReferralRecordServiceImpl.java
38. ReplyServiceImpl.java
39. RevenueDailyReportServiceImpl.java
40. ServiceItemServiceImpl.java
41. ServiceTrackServiceImpl.java
42. StatsServiceImpl.java
43. SysUserServiceImpl.java
44. TokenBlacklistService.java
45. WorkOrderServiceImpl.java

### AI服务 (service/ai/)
1. AiModelHealthIndicator.java
2. AiModelService.java
3. DeepSeekAiService.java

### 关键词匹配 (service/matcher/)
1. KeywordMatcherService.java
2. KeywordMatcherServiceImpl.java