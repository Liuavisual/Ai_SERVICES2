---
name: "alibaba-dev-standards"
description: "Enforces Alibaba Java Development Guidelines for this project. Invoke BEFORE writing any Java code, during code review, or when refactoring existing code to ensure compliance."
---

# Delta AI Customer Service - Alibaba Development Standards

This skill defines the mandatory coding standards, code review criteria, and best practices for the Delta AI Customer Service project, derived from Alibaba Java Development Guidelines and adapted to this project's architecture.

## When to Apply

- **Before writing any new Java code** — read relevant sections first
- **During code review** — use as checklist
- **When refactoring** — verify compliance after changes
- **When creating new modules/files** — follow naming and structure conventions

---

## 1. Naming Conventions

### 1.1 Class Naming

| Type | Format | Example | Counter-Example |
|------|--------|---------|-----------------|
| Constant class | `XxxConstants` (final class, private constructor) | `BusinessStatusConstants` | `BusinessStatusConstants` as interface |
| Service interface | `XxxService` | `CustomerService` | `CustomerServiceImpl` for interface |
| Service implementation | `XxxServiceImpl` | `CustomerServiceImpl` | `CustomerService` for impl |
| Controller | `XxxController` | `CustomerController` | `CustomerCtrl` |
| DTO | `XxxDTO` | `LoginDTO` | `LoginDto`, `LoginData` |
| VO | `XxxVO` | `CustomerVO` | `CustomerVo`, `CustomerView` |
| Mapper | `XxxMapper` | `CustomerMapper` | `CustomerDao` |
| Entity | Domain noun | `Customer`, `CompanionLevel` | `CustomerEntity`, `TbCustomer` |
| Exception | `XxxException` | `BusinessException` | `BizError` |
| Abstract class | `BaseXxx` or `AbstractXxx` | `BaseMessageProcessService` | `MessageProcessBase` |
| Enum | Domain noun or `XxxEnum` | `ResponseSource` | `ResponseSourceEnum` |

### 1.2 Method Naming

| Action | Prefix | Example |
|--------|--------|---------|
| Query single | `get`/`find`/`query` | `getCustomerById` |
| Query list | `list`/`get`+plural | `getAllEnabled` |
| Query page | `getPage`/`getPageXxx` | `getCustomerPage` |
| Count | `count`/`getCount` | `getPendingCount` |
| Insert | `save`/`insert`/`add`/`create` | `createKeyword` |
| Update | `update`/`modify` | `updateProfile` |
| Delete | `delete`/`remove` | `deleteUser` |
| Boolean check | `is`/`has`/`should` | `isValidStatus`, `isBlacklisted` |

### 1.3 Variable & Constant Naming

- Constants: UPPER_SNAKE_CASE, e.g. `PENDING_STATUS_PENDING`
- Variables: camelCase, e.g. `currentUserId`
- Boolean fields: avoid `is` prefix in POJO (Lombok generates `isXxx()` for `Boolean`), use `enabled`/`disabled` instead of `isEnabled`
- Package names: lowercase, no underscores, e.g. `com.delta.common.constant`

### 1.4 Project-Specific Naming

```
com.delta.common.constant    → XxxConstants (final class, NOT interface)
com.delta.common.entity      → Domain nouns, extend BaseEntity
com.delta.common.dto         → XxxDTO (inbound data)
com.delta.common.vo          → XxxVO (outbound data)
com.delta.common.mapper      → XxxMapper (MyBatis-Plus)
com.delta.common.service     → XxxService (interface)
com.delta.common.service.impl → XxxServiceImpl
com.delta.common.exception   → XxxException
com.delta.common.util        → XxxUtils / XxxHelper
com.delta.admin.controller   → XxxController extends BaseController
com.delta.admin.config       → XxxConfig / SecurityConfig
```

---

## 2. Constant Management (CRITICAL)

### 2.1 Mandatory Rules

- **NEVER use magic strings or numbers** — always define constants
- **NEVER use interface for constants** — use `final class` with `private` constructor
- **Group constants by domain** — one constants class per domain

### 2.2 Existing Constant Classes (MUST USE)

| Class | Domain | Key Constants |
|-------|--------|---------------|
| `BusinessStatusConstants` | Business status | PENDING_STATUS_*, SCHEDULE_STATUS_*, ASSIGN_STATUS_*, SERVICE_CATEGORY_*, PRICE_UNIT_*, ORDER_STATUS_*, ROLE_*, ENABLED_INT/DISABLED_INT |
| `AiCustomerServiceConstants` | AI service | ORDER_INTENT_KEYWORDS, HUMAN_EXPLICIT_KEYWORDS, NEGATIVE_EMOTION_KEYWORDS, AI_TIMEOUT_MS, reply templates (WAITING_REPLY, etc.) |
| `AiPersonalityConstants` | AI personality | PROFESSIONAL/CASUAL/ANCIENT/SECOND_DIMENSION, DEFAULT_PERSONALITY |
| `CustomerProfileConstants` | Customer profiling | RFM_SEGMENT_*, LIFECYCLE_*, MEMBER_LEVEL_*, RISK_LEVEL_*, SPENDING_TREND_*, NEED_TYPE_* |
| `ExportConstants` | Export & limits | EXPORT_PAGE_SIZE(10000), EXPORT_PAGE_NUM(1), TRUNCATION_LENGTHS, BEARER_PREFIX_LENGTH(7) |
| `MessageConstants` | Message | DIRECTION_IN/OUT, ROLE_USER/ASSISTANT |
| `PlatformConstants` | Platform | WECHAT/KOOK/YY |

### 2.3 When Adding New Constants

1. Check existing constant classes first — do NOT duplicate
2. If new domain, create new `XxxConstants` final class
3. Group related constants together with clear naming
4. Use `Collections.unmodifiableList()` for list constants
5. Use `BigDecimal.valueOf()` for decimal constants, NOT `new BigDecimal()`

### 2.4 Common Patterns to Replace

```java
// BAD - Magic values
if ("CS_STAFF".equals(role)) { ... }
wrapper.eq(Entity::getEnabled, 1);
Page<Entity> page = service.getPage(1, 10000, ...);
return "正在为您安排客服，请稍等片刻~";

// GOOD - Constants
if (BusinessStatusConstants.ROLE_CS_STAFF.equals(role)) { ... }
wrapper.eq(Entity::getEnabled, BusinessStatusConstants.ENABLED_INT);
Page<Entity> page = service.getPage(ExportConstants.EXPORT_PAGE_NUM, ExportConstants.EXPORT_PAGE_SIZE, ...);
return AiCustomerServiceConstants.WAITING_REPLY;
```

---

## 3. Exception Handling

### 3.1 Mandatory Rules

- **NEVER throw `RuntimeException` for business errors** — use `BusinessException`
- **NEVER swallow exceptions silently** — at minimum log at WARN level
- **NEVER catch `Exception` without logging** — always log or rethrow
- **NEVER use `e.printStackTrace()`** — use SLF4J logger

### 3.2 Exception Hierarchy

```
BusinessException (code + message)
├── Used for: expected business errors (validation, state violation, etc.)
├── Constructor: new BusinessException("message") → code=500
└── Constructor: new BusinessException(code, "message") → custom code

RuntimeException
└── Used for: ONLY system/infrastructure errors that should not be business errors
    ├── DistributedLockService timeout/interrupt → BusinessException
    └── Any other business-relevant failure → BusinessException
```

### 3.3 Exception Patterns

```java
// BAD
catch (Exception e) {
    // swallowed
}

// BAD
catch (Exception e) {
    e.printStackTrace();
}

// BAD
throw new RuntimeException("获取分布式锁超时: " + key);

// GOOD
catch (Exception e) {
    log.warn("操作失败: {}", e.getMessage());
    return defaultValue;
}

// GOOD
throw new BusinessException("获取分布式锁超时: " + key);

// GOOD - for truly unrecoverable infrastructure errors
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new BusinessException("操作被中断: " + key);
}
```

### 3.4 Global Exception Handler

`GlobalExceptionHandler` handles:
- `BusinessException` → `Result.error(code, message)`
- `MethodArgumentNotValidException` / `BindException` → validation error messages
- `Exception` (catch-all) → `Result.error("系统异常，请联系管理员")`

---

## 4. Code Structure & Method Design

### 4.1 Method Size Limits

- **Maximum 80 lines per method** — if longer, extract sub-methods
- **Maximum 5 parameters per method** — if more, use DTO/Map
- **Maximum 3 levels of nesting** — if deeper, extract method or use early return

### 4.2 Controller Rules

- **Controllers MUST NOT contain business logic** — only parameter extraction, delegation, response wrapping
- **Use `BaseController` for common methods** — `getCurrentUserId()`, `getCurrentUserRole()`
- **Role checking MUST use constants** — `BusinessStatusConstants.ROLE_CS_STAFF`, not `"CS_STAFF"`
- **Export methods MUST use `ExportConstants`** — not hardcoded page sizes

```java
// BAD - Business logic in controller
String role = (String) request.getAttribute("role");
if ("CS_STAFF".equals(role)) {
    Long currentUserId = (Long) request.getAttribute("userId");
    csUserId = currentUserId;
}

// GOOD - Delegate to service, use constants
String role = getCurrentUserRole(request);
if (BusinessStatusConstants.ROLE_CS_STAFF.equals(role)) {
    csUserId = getCurrentUserId(request);
}
```

### 4.3 Service Rules

- **Service interfaces define contracts** — implementation details in `XxxServiceImpl`
- **Use `@Transactional` for write operations** — especially multi-table operations
- **Template Method pattern** — `BaseMessageProcessService` for multi-platform message processing
- **Avoid N+1 queries** — use batch queries + Map grouping instead of loop-per-item queries

### 4.4 Method Decomposition Pattern

```java
// BAD - 140-line method
public void refreshProfile(Long userId) {
    // 140 lines of mixed logic...
}

// GOOD - Decomposed with clear sub-methods
public void refreshProfile(Long userId) {
    CustomerProfile p = getOrCreateProfile(userId);
    updateOrderMetrics(p);
    updateInteractionMetrics(p);
    calculateRfmSegment(p);
    calculateSpendingTrend(p);
    calculateSatisfactionTrend(p);
    calculateLifecycleStage(p, LocalDateTime.now());
    calculateMemberLevel(p);
    calculateRiskLevel(p);
    calculateNeedType(p);
    customerProfileMapper.updateById(p);
}
```

---

## 5. Performance Standards

### 5.1 Database Queries

- **NEVER query in loops (N+1 problem)** — use batch queries + Map grouping
- **Use `Set` instead of `List` for `contains()` checks** — O(1) vs O(n)
- **Use SQL aggregation for statistics** — not in-memory calculation
- **Always use pagination for large datasets** — never load all records

```java
// BAD - N+1 query
for (CompanionLevel level : levels) {
    List<ServicePriceRule> rules = mapper.selectList(
        new LambdaQueryWrapper<>().eq(ServicePriceRule::getCompanionLevelId, level.getId()));
}

// GOOD - Batch query + Map grouping
List<Long> levelIds = levels.stream().map(CompanionLevel::getId).collect(Collectors.toList());
List<ServicePriceRule> allRules = mapper.selectList(
    new LambdaQueryWrapper<>().in(ServicePriceRule::getCompanionLevelId, levelIds));
Map<Long, List<ServicePriceRule>> rulesByLevel = allRules.stream()
    .collect(Collectors.groupingBy(ServicePriceRule::getCompanionLevelId));
```

### 5.2 Collection Operations

```java
// BAD - List.contains() in loop
List<Long> ids = assignments.stream().map(CsUserCustomer::getCustomerUserId).collect(Collectors.toList());
for (User user : assignedUsers) {
    if (!ids.contains(user.getId())) { ids.add(user.getId()); }
}

// GOOD - Set for deduplication
Set<Long> idSet = assignments.stream().map(CsUserCustomer::getCustomerUserId)
    .collect(Collectors.toCollection(LinkedHashSet::new));
for (User user : assignedUsers) { idSet.add(user.getId()); }
return new ArrayList<>(idSet);
```

### 5.3 Export Operations

- **Use `ExportConstants.EXPORT_PAGE_SIZE`** for export page size (10000)
- **Desensitize sensitive data** in exports (phone, wechat, etc.)
- **Consider streaming for very large datasets** instead of loading all into memory

---

## 6. Security Standards

### 6.1 Authentication & Authorization

- **NEVER accept tokens via URL query parameters** — only via `Authorization: Bearer` header
- **Use `ExportConstants.BEARER_PREFIX_LENGTH`** for Bearer token extraction
- **Role-based access control** — three tiers: `SYS_ADMIN` > `CS_LEADER` > `CS_STAFF`
- **Data isolation for CS_STAFF** — can only see own assigned customers

### 6.2 Data Protection

- **Desensitize sensitive data in exports** — phone: `1****8`, wechat: `w***t`
- **NEVER hardcode passwords or API keys** — use configuration properties
- **NEVER log sensitive data** — passwords, tokens, personal information
- **Use `@PreAuthorize` for endpoint authorization** — not manual role checking in service

### 6.3 Input Validation

- **Always use `@Valid` + Jakarta Validation** for request DTOs
- **Validate business rules in service layer** — not just format validation
- **Use `@NotNull`, `@NotBlank`, `@Size`** for DTO field constraints

---

## 7. MyBatis-Plus Conventions

### 7.1 Query Building

```java
// Use LambdaQueryWrapper - type-safe, refactor-friendly
LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Customer::getPlatform, PlatformConstants.WECHAT);
wrapper.eq(Customer::getDeleted, BusinessStatusConstants.NOT_DELETED);
wrapper.orderByDesc(Customer::getCreatedAt);

// NEVER use string-based column names
// BAD: wrapper.eq("platform", "wechat");
```

### 7.2 Entity Design

- **Extend `BaseEntity`** for common fields (id, createdAt, updatedAt)
- **Use `@TableName`** for table mapping
- **Use `@TableId(type = IdType.AUTO)`** for auto-increment ID (in BaseEntity)
- **Use `@TableField(fill = FieldFill.INSERT)`** for auto-fill timestamps (in BaseEntity)
- **Integer type for enabled/disabled** — `1` = enabled, `0` = disabled (use constants)

---

## 8. Code Review Checklist

Before submitting any code, verify:

### Naming
- [ ] No magic strings/numbers — all values use constants
- [ ] Class names follow conventions (XxxService, XxxServiceImpl, XxxDTO, XxxVO)
- [ ] Method names are descriptive and follow verb conventions
- [ ] Constants are in `final class` with `private` constructor, NOT interface

### Structure
- [ ] Method length ≤ 80 lines
- [ ] Parameter count ≤ 5
- [ ] Nesting depth ≤ 3 levels
- [ ] No business logic in controllers
- [ ] Controller extends `BaseController` for common methods

### Exception Handling
- [ ] No `RuntimeException` for business errors — use `BusinessException`
- [ ] No swallowed exceptions — at minimum `log.warn()`
- [ ] No `e.printStackTrace()` — use SLF4J logger
- [ ] `InterruptedException` restores interrupt status

### Performance
- [ ] No N+1 queries — batch queries + Map grouping
- [ ] `Set` for `contains()` operations, not `List`
- [ ] Pagination for large dataset queries
- [ ] Export uses `ExportConstants.EXPORT_PAGE_SIZE`

### Security
- [ ] No tokens via URL parameters
- [ ] Sensitive data desensitized in exports
- [ ] No hardcoded passwords/API keys
- [ ] `@PreAuthorize` on endpoints, role constants used
- [ ] CS_STAFF data isolation enforced

### MyBatis-Plus
- [ ] `LambdaQueryWrapper` used, not string-based
- [ ] Entity extends `BaseEntity`
- [ ] Enabled/disabled uses `BusinessStatusConstants.ENABLED_INT/DISABLED_INT`

---

## 9. Project Architecture Reference

```
delta-ai-customer-service (parent POM)
├── delta-common          → Entities, DTOs, VOs, Services, Mappers, Constants, Utils
├── delta-message         → AI integration (DeepSeekServiceImpl)
├── delta-platform        → Platform adapters (WeChatMessageServiceImpl)
└── delta-admin           → Controllers, SecurityConfig, JwtAuthenticationFilter

Key Patterns:
- Template Method: BaseMessageProcessService → WeChatMessageServiceImpl
- Strategy: KeywordMatcherService → KeywordMatcherServiceImpl
- Observer: AdminNotificationHandler → WebSocket notifications
- DTO/VO separation: Inbound DTOs, Outbound VOs, never expose entities directly
```

### Key Service Flow

```
Incoming Message → Platform Adapter (WeChat/KOOK)
  → BaseMessageProcessService.processTextMessage()
    → Keyword matching (KeywordMatcherService)
    → Reply lookup (ReplyService)
    → AI generation (DeepSeekService)
    → Pending message creation (PendingMessageService)
    → Admin notification (AdminNotificationHandler)
  → Response sent back to platform
```

---

## 10. Prohibited Patterns

These patterns are **strictly forbidden** in this project:

```java
// 1. Magic values
if ("pending".equals(status)) { }           // Use BusinessStatusConstants.PENDING_STATUS_PENDING
wrapper.eq(Entity::getEnabled, 1);           // Use BusinessStatusConstants.ENABLED_INT
Page<Entity> p = service.getPage(1, 10000);  // Use ExportConstants.EXPORT_PAGE_NUM/EXPORT_PAGE_SIZE

// 2. Interface for constants
public interface MyConstants { String VALUE = "x"; }  // Use final class with private constructor

// 3. RuntimeException for business errors
throw new RuntimeException("操作失败");       // Use BusinessException

// 4. Swallowed exceptions
catch (Exception e) { /* nothing */ }         // At minimum: log.warn()

// 5. e.printStackTrace()
e.printStackTrace();                          // Use log.error("description", e)

// 6. String-based query columns
wrapper.eq("status", "active");               // Use LambdaQueryWrapper: wrapper.eq(Entity::getStatus, ...)

// 7. Business logic in controllers
if (role.equals("ADMIN")) { doBusiness(); }   // Move to service layer

// 8. URL query parameter tokens
request.getParameter("token");                // Use Authorization header only

// 9. Hardcoded passwords/keys
private String password = "123456";           // Use configuration properties

// 10. N+1 queries
for (Entity e : list) { dao.selectById(e.getId()); }  // Use batch query + Map
```
