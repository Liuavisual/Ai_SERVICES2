package com.delta.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义权限校验注解
 * <p>
 * 用于替代Spring Security的 @PreAuthorize 硬编码角色校验。
 * 支持权限编码格式：resource:action，如 customer:view、companion:edit。
 * </p>
 * <p>使用示例：
 * <pre>
 *   @PermAuth("companion:edit")
 *   public Result<Void> updateCompanion(...) { ... }
 *
 *   @PermAuth({"customer:view", "customer:assign"})
 *   public Result<Page<CustomerVO>> listCustomers(...) { ... }
 * </pre>
 * value为空或 ["*"] 表示仅需登录，不校验具体权限。
 * </p>
 *
 * @author 刘建国
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermAuth {

    /**
     * 所需的权限编码列表
     * 多个权限时，满足任意一个即可（OR逻辑）
     */
    String[] value() default {};

    /**
     * 校验模式
     * ANY - 拥有任一权限即可（默认）
     * ALL - 必须同时拥有所有权限
     */
    MatchMode mode() default MatchMode.ANY;

    enum MatchMode {
        ANY, ALL
    }
}