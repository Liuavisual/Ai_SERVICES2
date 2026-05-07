package com.delta.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * <p>
 * 标注在Controller方法上，用于声明该方法需要哪些权限。
 * 使用 @RequirePermission("customer:edit") 声明单个权限，
 * 使用 @RequirePermission({"customer:edit", "customer:view"}) 声明需要同时满足多个权限。
 * </p>
 * <p>
 * 特殊规则：
 * <ul>
 *   <li>SYS_ADMIN 内置角色拥有所有权限，无需额外配置</li>
 *   <li>CS_LEADER 拥有其管理范围内的所有 view/edit 权限</li>
 *   <li>空数组或不标注表示公开接口（由SecurityConfig控制）</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 需要的权限编码列表
     *
     * @return 权限编码数组
     */
    String[] value() default {};
}
