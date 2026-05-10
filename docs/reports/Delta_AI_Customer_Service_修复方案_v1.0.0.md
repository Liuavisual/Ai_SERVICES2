# Delta AI Customer Service 系统性修复方案

> **版本**: v1.0.0 | **日期**: 2026-05-08 | **负责人**: 刘建国
> **依据**: 四份测试报告（综合测试报告、后端审查报告、业务流程报告、数据库分析报告）

---

## 目录

1. [修复总览](#一修复总览)
2. [第一阶段：紧急修复 P0（1周内）](#二第一阶段紧急修复-p01周内)
3. [第二阶段：核心修复 P1（2-4周）](#三第二阶段核心修复-p12-4周)
4. [第三阶段：增强修复 P2（1-2月）](#四第三阶段增强修复-p21-2月)
5. [第四阶段：优化修复 P3（2-3月）](#五第四阶段优化修复-p32-3月)
6. [修复验证清单](#六修复验证清单)

---

## 一、修复总览

### 1.1 问题分布统计

| 阶段 | 优先级 | 问题数 | 核心修复项 | 预计工时 |
|------|--------|--------|------------|----------|
| 第一阶段 | **P0 致命** | 1 | RedissonConfig 密码 Bug | 2h |
| 第二阶段 | **P1 严重** | 27 | 异常处理、权限体系、前端核心 | 40h |
| 第三阶段 | **P2 一般** | 57 | HashMap容量、注释、代码风格 | 60h |
| 第四阶段 | **P3 轻微** | 39 | 注解顺序、import整理、细节优化 | 30h |
| **合计** | | **124** | | **132h** |

### 1.2 修复原则

- **最小影响原则**: 每次修改仅影响目标代码，不引入新依赖
- **向后兼容原则**: 修改不改变现有 API 契约和行为
- **渐进交付原则**: 每阶段独立可交付，不依赖后续阶段
- **验证驱动原则**: 每项修复后必须通过编译和现有测试

---

## 二、第一阶段：紧急修复 P0（1周内）

### 🔴 FIX-001: RedissonConfig 密码配置 Bug

**文件**: [RedissonConfig.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/config/RedissonConfig.java) L35-L48
**严重程度**: 致命 | **影响**: 系统所有分布式锁全部失效 | **工时**: 2h

**问题根因**:
`config.useSingleServer()` 每次调用都返回一个**全新**的 `SingleServerConfig` 对象。第35行设置的连接池参数在第47行被新对象覆盖。

**当前代码（错误）**:
```java
config.useSingleServer()
        .setAddress(address)
        .setDatabase(database)
        .setConnectionMinimumIdleSize(10)
        .setConnectionPoolSize(64)
        .setIdleConnectionTimeout(10000)
        .setConnectTimeout(10000)
        .setTimeout(3000)
        .setRetryAttempts(3)
        .setRetryInterval(1500);

if (password != null && !password.isEmpty()) {
    config.useSingleServer().setPassword(password);  // ⚠️ BUG: 创建新对象!
}
```

**修复代码**:
```java
org.redisson.config.SingleServerConfig serverConfig = config.useSingleServer()
        .setAddress(address)
        .setDatabase(database)
        .setConnectionMinimumIdleSize(10)
        .setConnectionPoolSize(64)
        .setIdleConnectionTimeout(10000)
        .setConnectTimeout(10000)
        .setTimeout(3000)
        .setRetryAttempts(3)
        .setRetryInterval(1500);

if (password != null && !password.isEmpty()) {
    serverConfig.setPassword(password);
}
```

**验证方法**:
1. 编译通过：`mvn compile -pl delta-common`
2. 启动应用检查 Redisson 连接池日志
3. 单元测试：调用 `DistributedLockService.tryLock()` 验证锁功能正常

---

## 三、第二阶段：核心修复 P1（2-4周）

---

### 🔴 FIX-002: 修复 catch(Throwable) 为 catch(Exception)

**文件1**: [AuditLogAspect.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/aspect/AuditLogAspect.java) L87
**文件2**: [ProtectionAspect.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/aspect/ProtectionAspect.java) L104
**严重程度**: 严重 | **影响**: 可能吞掉 JVM 级 Error | **工时**: 1h

**当前代码**:
```java
// AuditLogAspect.java L87
} catch (Throwable t) {
    error = t;
    throw t;
}

// ProtectionAspect.java L104
} catch (Throwable t) {
    error = t;
    throw t;
}
```

**修复代码**:
```java
// AuditLogAspect.java L87
} catch (Exception e) {
    error = e;
    throw e;
}

// ProtectionAspect.java L104
} catch (Exception e) {
    error = e;
    throw e;
}
```

同时更新方法签名中的 `@throws Throwable` 为 `@throws Exception`。

**验证**: `mvn compile -pl delta-common`，确保编译通过。

---

### 🔴 FIX-003: CustomerProfile @EqualsAndHashCode 修正

**文件**: [CustomerProfile.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/entity/CustomerProfile.java) L23
**严重程度**: 严重 | **影响**: 实体 equals/hashCode 不包含 id，影响集合操作 | **工时**: 0.5h

**当前代码**:
```java
@EqualsAndHashCode(callSuper = false)
```

**修复代码**:
```java
@EqualsAndHashCode(callSuper = true)
```

**验证**: `mvn compile -pl delta-common`

---

### 🔴 FIX-004: RateLimiter 并发安全修复

**文件**: [RateLimiter.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/util/RateLimiter.java) L19-L32
**严重程度**: 严重 | **影响**: 高并发时限流计数不准确 | **工时**: 3h

**当前代码**:
```java
public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
    String redisKey = RATE_LIMIT_PREFIX + key;
    String countStr = redisTemplate.opsForValue().get(redisKey);
    if (countStr == null) {
        redisTemplate.opsForValue().set(redisKey, "1", windowSeconds, TimeUnit.SECONDS);
        return true;
    }
    int count = Integer.parseInt(countStr);
    if (count >= maxRequests) {
        return false;
    }
    redisTemplate.opsForValue().increment(redisKey);
    return true;
}
```

**修复代码**（使用 Redis Lua 脚本确保原子性）:
```java
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.Collections;
import java.util.List;

/**
 * Redis Lua 脚本：原子性递增并检查限流
 * KEYS[1] = Redis Key
 * ARGV[1] = 最大请求数
 * ARGV[2] = 时间窗口（秒）
 * 返回值 > 0 表示允许通过，<= 0 表示被限流
 */
private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

static {
    RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
    RATE_LIMIT_SCRIPT.setResultType(Long.class);
    RATE_LIMIT_SCRIPT.setScriptText(
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local window = tonumber(ARGV[2]) " +
        "local current = redis.call('INCR', key) " +
        "if current == 1 then " +
        "    redis.call('EXPIRE', key, window) " +
        "end " +
        "if current > limit then " +
        "    return 0 " +
        "end " +
        "return 1"
    );
}

public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
    String redisKey = RATE_LIMIT_PREFIX + key;
    List<String> keys = Collections.singletonList(redisKey);
    Long result = redisTemplate.execute(
        RATE_LIMIT_SCRIPT,
        keys,
        String.valueOf(maxRequests),
        String.valueOf(windowSeconds)
    );
    return result != null && result > 0;
}
```

**验证**: 单元测试模拟高并发场景，验证限流准确性。

---

### 🔴 FIX-005: DTO 移除冗余 getter/setter

**文件1**: [LoginDTO.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/dto/LoginDTO.java) L27-L49
**文件2**: [RegisterDTO.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/dto/RegisterDTO.java) L41-L79
**严重程度**: 严重 | **影响**: 代码冗余，维护成本高 | **工时**: 2h

**说明**: `@Data` 注解已自动生成所有字段的 getter/setter，显式声明完全冗余。

**修复操作**（以 LoginDTO 为例）:

删除 L27-L49（所有显式 getter/setter）:
```java
// 删除以下全部内容
public String getUsername() { return username; }
public void setUsername(String username) { this.username = username; }
public String getPassword() { return password; }
public void setPassword(String password) { this.password = password; }
public String getClientIp() { return clientIp; }
public void setClientIp(String clientIp) { this.clientIp = clientIp; }
```

同时对 RegisterDTO 和以下文件执行同样操作:
- `OrderCreateDTO.java`
- `PlatformConfigDTO.java`
- 其他存在冗余 getter/setter 的 DTO

**验证**: `mvn compile -pl delta-common`，确保所有引用处编译通过。

---

### 🔴 FIX-006: 前端 keep-alive 缓存修复

**文件1**: [MainLayout.vue](file:///d:/Project/AI-SERVERS/delta-ui/src/layouts/MainLayout.vue) L253-L261
**文件2**: [router/index.js](file:///d:/Project/AI-SERVERS/delta-ui/src/router/index.js) (路由配置文件)
**严重程度**: 严重 | **影响**: keep-alive 组件缓存完全无效 | **工时**: 3h

**问题根因**: `collectCachedViews()` 收集带有 `meta.keepAlive` 的路由名称，但路由配置中**没有任何路由**定义了 `meta.keepAlive = true`。

**修复方案1（推荐）**: 为核心页面路由添加 `meta.keepAlive`

在路由配置中，对需要缓存的页面（如Dashboard、Customer列表等）添加:
```javascript
{
  path: '/dashboard',
  name: 'Dashboard',
  component: () => import('@/views/Dashboard.vue'),
  meta: { title: '工作台', keepAlive: true, roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
},
{
  path: '/customers',
  name: 'Customer',
  component: () => import('@/views/Customer.vue'),
  meta: { title: '客户管理', keepAlive: true, roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
},
```

**修复方案2（备选）**: 如果不需要 keep-alive，移除无效代码

MainLayout.vue L66 修改 `<keep-alive>` 为直接渲染:
```html
<!-- 移除 keep-alive 包装 -->
<component :is="Component" :key="$route.fullPath" />
```

同时移除 `collectCachedViews()` 函数（L253-L261）和 `onMounted` 中的调用（L263-L265）。

**验证**: 运行 `npm run dev`，切换页面检查组件是否复用。

---

### 🔴 FIX-007: 前端 Login.vue 绕过 Pinia Store 修复

**文件**: [Login.vue](file:///d:/Project/AI-SERVERS/delta-ui/src/views/Login.vue) L119-L131
**严重程度**: 严重 | **影响**: 状态不一致，绕过 Pinia 状态管理 | **工时**: 2h

**当前代码（绕过 Pinia）**:
```typescript
// L122-L129
const res = await authApi.login(loginForm)
if (res.code === 200) {
  const data = res.data as LoginVO
  authStorage.setAuth(data as unknown as Record<string, unknown>)  // ❌ 绕过 Store
  ElMessage.success('登录成功')
  const redirect = route.query.redirect as string
  if (redirect && redirect !== '/login') {
    router.push(redirect)
  } else {
    router.push(authStorage.getRoleHomePage(data.role as UserRole))
  }
}
```

**修复代码（经过 Pinia Store）**:
```typescript
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const handleLogin = async (): Promise<void> => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const success = await authStore.login(loginForm)  // ✅ 使用 Pinia Store
    if (success) {
      ElMessage.success('登录成功')
      const redirect = route.query.redirect
      if (redirect && typeof redirect === 'string' && redirect !== '/login') {
        router.push(redirect)
      } else {
        router.push(authStore.roleHomePage)
      }
    }
  } catch (error: unknown) {
    const err = error as { response?: { status?: number } }
    if (!err.response) {
      ElMessage.error('网络连接异常，请检查网络后重试')
    }
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}
```

**同时需要修改 `catch (error: any)` 为 `catch (error: unknown)`** 以消除 TypeScript any 类型。

**验证**: 运行 `npm run dev`，测试登录流程，确保 token 和 userInfo 存储正确。

---

### 🔴 FIX-008: 前端 Axios 拦截器 method 大小写 Bug

**文件**: [request.ts](file:///d:/Project/AI-SERVERS/delta-ui/src/utils/request.ts) L64
**严重程度**: 严重 | **影响**: 请求方法为 'POST' 等大写时匹配失败 | **工时**: 0.5h

**当前代码**:
```typescript
if (['post', 'put', 'patch'].includes(config.method || '') && config.data === undefined) {
```

**修复代码**:
```typescript
const method = (config.method || '').toLowerCase()
if (['post', 'put', 'patch'].includes(method) && config.data === undefined) {
```

**验证**: `npm run dev`，发起 POST/PUT 请求验证。

---

### 🔴 FIX-009: 前端路由守卫 userInfo.role null 安全检查

**文件**: [router/index.js](file:///d:/Project/AI-SERVERS/delta-ui/src/router/index.js) L295-L302
**严重程度**: 严重 | **影响**: userInfo 为 `{}` 时 `userInfo.role` 为 undefined，导致崩溃 | **工时**: 0.5h

**当前代码**:
```javascript
if (to.path === '/login') {
    const userInfo = authStorage.getUserInfo()
    next(authStorage.getRoleHomePage(userInfo.role))
    return
}
// ...
const userInfo = authStorage.getUserInfo()
if (!to.meta.roles.includes(userInfo.role)) {
```

**修复代码**:
```javascript
if (to.path === '/login') {
    const userInfo = authStorage.getUserInfo()
    if (userInfo?.role) {
        next(authStorage.getRoleHomePage(userInfo.role))
    } else {
        next()  // 无角色信息，放行到登录页
    }
    return
}
// ...
const userInfo = authStorage.getUserInfo()
if (!userInfo?.role || (to.meta?.roles && !to.meta.roles.includes(userInfo.role))) {
```

**验证**: 清除 localStorage 后刷新页面，确保不崩溃。

---

### 🔴 FIX-010: 前端 TypeScript strict 模式开启

**文件**: [tsconfig.json](file:///d:/Project/AI-SERVERS/delta-ui/tsconfig.json) L4
**工时**: 3h（需逐个修复 strict 模式下的类型错误）

**第一步**: 修改 tsconfig.json
```json
{
  "compilerOptions": {
    "strict": true,
    // ...
  }
}
```

**第二步**: 运行 `npx vue-tsc --noEmit`，逐个修复类型错误：

1. **ErrorBoundary.vue L42** - `$options` 在 Composition API 不可用:
```typescript
// 移除 instance?.$options?.name，改为
const errorInfo = ref<string>('')
onErrorCaptured((err, instance, info) => {
  errorInfo.value = info
  // ...
})
```

2. **OverviewCard.icon 类型**: `icon: any` → `icon: Component`
```typescript
import type { Component } from 'vue'
interface OverviewCard {
  icon: Component  // 替换 any
}
```

3. **Login.vue `catch (error: any)`** → `catch (error: unknown)`

4. **downloadExcel.ts L23** - Blob 类型断言:
```typescript
// 替换 res as unknown as Blob
const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
```

---

## 四、第三阶段：增强修复 P2（1-2月）

---

### 🟡 FIX-011: 统一权限控制方案

**涉及文件**: [PermissionInterceptor.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/interceptor/PermissionInterceptor.java), 37 个 Controller
**工时**: 8h

**方案**: 统一使用 `@PreAuthorize`（Spring Security 原生支持），移除未使用的 `@RequirePermission` 注解和 `PermissionInterceptor`。

**操作步骤**:
1. 确认所有 Controller 的 `@PreAuthorize` 注解完整
2. 移除 [PermissionController.java](file:///d:/Project/AI-SERVERS/delta-admin/src/main/java/com/delta/admin/controller/PermissionController.java) L41 的 `@RequirePermission`，替换为 `@PreAuthorize("hasRole('SYS_ADMIN')")`
3. 从 `WebConfig.java` 中移除 `PermissionInterceptor` 注册
4. 删除或标记 `@RequirePermission` 注解和 `PermissionInterceptor` 为 `@Deprecated`

---

### 🟡 FIX-012: 敏感词检测增加 WARNING 级别

**文件**: [ContentSafetyServiceImpl.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/service/impl/ContentSafetyServiceImpl.java)
**工时**: 4h

**新增枚举值**:
```java
public enum SafetyLevel {
    SAFE,     // 安全
    WARNING,  // 新增：疑似违规（上下文敏感，如问句中的敏感词）
    BLOCK     // 确认违规
}
```

**逻辑调整**: 当检测到敏感词但消息上下文为问句模式时，降级为 WARNING 级别。

---

### 🟡 FIX-013: 消息处理去重机制

**文件**: [BaseMessageProcessService.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/service/impl/BaseMessageProcessService.java)
**工时**: 3h

**实现**: 在消息处理前，计算消息内容的 MD5，通过 Redis `SETNX` 设置 60s 窗口的去重键。

```java
// 消息去重，60秒内相同内容不重复处理
private boolean isDuplicate(String content) {
    String md5 = SecureUtil.md5(content);
    String dedupKey = "message:dedup:" + md5;
    Boolean success = redisTemplate.opsForValue()
        .setIfAbsent(dedupKey, "1", 60, TimeUnit.SECONDS);
    return success != null && !success;
}
```

---

### 🟡 FIX-014: HashMap 指定初始容量

**涉及文件**: 约 15 个文件
**工时**: 3h

**批量修复**:
| 文件 | 行 | 当前 | 修复 |
|------|-----|------|------|
| JwtUtils.java | L27,L45,L55 | `new HashMap<>()` | `new HashMap<>(8)` |
| ServiceTrackServiceImpl.java | L57,L73,L93,L111,L140 | `new HashMap<>()` | `new HashMap<>(16)` |
| ServiceProtectionManager.java | L91,L318 | `new HashMap<>()` | `new HashMap<>(16)` |
| AutoTestRunner.java | L146 | `new HashMap<>()` | `new HashMap<>(8)` |
| AiPersonalityConstants.java | L133 | `new HashMap<>()` | `new HashMap<>(16)` |
| WeWorkApiServiceImpl.java | L76,L92,L95 | `new HashMap<>()` | `new HashMap<>(8)` |
| StatsServiceImpl.java | L329 | `new HashMap<>()` | `new HashMap<>(8)` |

**验证**: `mvn compile`，确保编译通过。

---

### 🟡 FIX-015: 注释格式标准化

**涉及文件**: ~40 Entity + ~33 DTO + ~43 VO
**工时**: 6h

**当前格式**:
```java
/** 用户名 */    private String username;
```

**标准格式**:
```java
/** 用户名 */
private String username;
```

**操作**: 逐文件使用 IDE 格式整理，注释移到字段上方独立一行。

---

### 🟡 FIX-016: 前端 uploadExcel Content-Type 修复

**文件**: [uploadExcel.ts](file:///d:/Project/AI-SERVERS/delta-ui/src/utils/uploadExcel.ts) L24
**工时**: 0.5h

**当前代码**:
```typescript
headers: { 'Content-Type': 'multipart/form-data' }
```

**修复**: 移除手动 Content-Type，让 axios 自动设置含 boundary 的正确值:
```typescript
// 直接删除 Content-Type header，axios 会自动处理
const formData = new FormData()
formData.append('file', file)
return http.post('/upload', formData)
```

---

### 🟡 FIX-017: DesensitizeUtils isSensitiveKey 修复

**文件**: [DesensitizeUtils.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/util/DesensitizeUtils.java) L48-L52
**工时**: 1h

**当前**: `contains("key")` 会误判 `"monkey"` → 改用 `equals()` 精确匹配或后缀匹配:
```java
private static boolean isSensitiveKey(String key) {
    if (key == null) return false;
    String lower = key.toLowerCase();
    // 精确匹配或后缀匹配
    return lower.equals("password") || lower.equals("secret")
        || lower.equals("token") || lower.endsWith("key")
        || lower.endsWith("secret") || lower.endsWith("password");
}
```

---

## 五、第四阶段：优化修复 P3（2-3月）

---

### 🟢 FIX-018: 数据库 Message 表分区策略

**数据库**: MySQL 8.0 `delta_ai_customer_service.messages`
**工时**: 8h（含验证）
**风险**: 需要对生产表执行 DDL，需在维护窗口操作

**SQL**:
```sql
ALTER TABLE messages PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

**配套定时任务**: 每月1日自动创建下月分区:
```java
@Component
public class MessagePartitionTask {
    @Scheduled(cron = "0 0 1 1 * ?")
    public void createNextMonthPartition() {
        // ALTER TABLE messages ADD PARTITION ...
    }
}
```

---

### 🟢 FIX-019: 消除定价三处冗余

**涉及**: ClubConfig, CompanionLevel, ClubLevelPrice
**工时**: 16h

**方案**:
1. 以 `CompanionLevel` + `ServicePriceRule` 作为唯一价格数据源
2. `ClubConfig` 移除 `priceLevelTwo/One/Top/Star` 字段，改为引用 `ClubLevelPrice`
3. `ClubLevelPrice` 改为视图，从 `CompanionLevel.basePrice` 动态读取
4. 更新所有引用旧价格字段的代码，重定向到新数据源

---

### 🟢 FIX-020: 关键词匹配算法升级

**文件**: [KeywordMatcherServiceImpl.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/service/matcher/KeywordMatcherServiceImpl.java)
**工时**: 8h

**当前**: `contains()` 遍历 O(n*m)
**升级**: 使用 Aho-Corasick（AC自动机）或 Trie 树，实现 O(n+m)

```java
// 引入 AC 自动机（项目已有 Hutool，可使用 WordTree）
import cn.hutool.dfa.WordTree;

private WordTree keywordTree = new WordTree();

public void rebuildTree(List<String> keywords) {
    keywordTree = new WordTree();
    keywordTree.addWords(keywords);
}

// O(n) 复杂度匹配
public List<String> matchAll(String text) {
    return keywordTree.matchAllWords(text, -1, false, false);
}
```

---

### 🟢 FIX-021: 订单号改为 Redis 自增序列

**文件**: [OrderServiceImpl.java](file:///d:/Project/AI-SERVERS/delta-common/src/main/java/com/delta/common/service/impl/OrderServiceImpl.java)
**工时**: 4h

**当前代码**:
```java
// 毫秒时间戳 + 4位随机数，有碰撞风险
String orderNo = "ORD" + System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));
```

**修复代码**（参考工单模块 WK + Redis 自增）:
```java
// 使用 Redis 自增序列 + 日期前缀
String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
String redisKey = "order:seq:" + datePrefix;
Long seq = redisTemplate.opsForValue().increment(redisKey);
redisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
String orderNo = "ORD" + datePrefix + String.format("%06d", seq);
```

---

### 🟢 FIX-022: 其他 P3 修复项

| 编号 | 内容 | 工时 |
|------|------|------|
| MIN-001 | Entity 注解顺序统一（@Data 在 @EqualsAndHashCode 之前） | 2h |
| MIN-002 | Controller @Operation 补充 summary | 2h |
| MIN-003 | Service 文件 import 整理 | 1h |
| MIN-004 | PasswordGenerator 工具类私有构造函数 | 0.5h |
| MIN-005 | ExcelUtils 跨平台编码确认 | 0.5h |
| MIN-006 | IdObfuscateUtils 算法说明注释 | 0.5h |
| MIN-007 | BusinessException 支持错误码参数 | 2h |
| MIN-008 | SecurityConfig CORS 生产环境限制具体域名 | 1h |
| MIN-009 | JwtConfig secret 长度 >= 256 bits 确认 | 0.5h |
| MIN-010 | JwtAuthenticationFilter Cookie 名称配置化 | 1h |
| MIN-011 | ServiceProtectionManager HashMap → Caffeine Cache | 3h |
| MIN-012 | CacheInitListener 预热数据量确认 | 1h |
| MIN-013 | 增加测试覆盖率（核心 Service） | 16h |

---

## 六、修复验证清单

### 6.1 编译验证

| 阶段 | 验证命令 | 预期结果 |
|------|----------|----------|
| 每阶段 | `mvn compile -pl delta-common,delta-admin,delta-message,delta-platform` | BUILD SUCCESS |
| 第二三四阶段 | `cd delta-ui && npx vue-tsc --noEmit` | 0 errors |

### 6.2 单元测试验证

| 阶段 | 验证命令 | 预期结果 |
|------|----------|----------|
| 后端 | `mvn test` | Tests run OK |
| 前端 | `npm run test` | Tests passed |

### 6.3 功能验证

| 验证项 | 验证方法 | 负责 |
|--------|----------|------|
| Redisson 分布式锁 | 启动应用 → 并发创建工单 → 检查工单号唯一 | 后端 |
| RateLimiter 限流准确 | 并发请求 → 统计 Redis 计数器 | 后端 |
| 登录流程 | Login.vue → 登录 → 检查 localStorage + Pinia DevTools | 前端 |
| keep-alive 缓存 | 切换页面 → Vue DevTools 检查组件是否复用 | 前端 |
| 权限控制 | 不同角色登录 → 检查菜单可见项 | 前端 |
| Message 分区查询 | EXPLAIN SELECT → 确认分区裁剪生效 | DBA |

### 6.4 灰度上线策略

| 阶段 | 上线策略 | 回滚方案 |
|------|----------|----------|
| P0 | 立即修复，紧急发版 | Git revert + 重新部署 |
| P1 | 分批测试环境 → 预发环境 → 生产 | 关闭 keep-alive 功能开关 |
| P2 | 常规迭代发布 | Git revert |
| P3 | 常规迭代发布（含 DBA 窗口） | 分区表有备份，可回滚 |

---

*方案生成时间: 2026-05-08 | 依据报告: 综合测试报告 v1.0.0 + 后端审查报告 + 业务流程报告 + 数据库分析报告*
