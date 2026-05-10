package com.delta.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 * <p>
 * 标注在需要记录审计日志的Controller方法上，配合AuditLogAspect切面
 * 自动记录操作模块、动作、用户、IP、耗时等信息。
 * </p>
 *
 * @author 刘建国
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 日志描述（备用字段）
     *
     * @return 描述信息
     */
    String value() default "";

    /**
     * 操作模块名称，如"认证"、"用户管理"
     *
     * @return 模块名称
     */
    String module() default "";

    /**
     * 操作动作名称，如"用户登录"、"创建用户"
     *
     * @return 动作名称
     */
    String action() default "";

    /**
     * 是否保存方法返回结果到日志
     *
     * @return true则记录返回值，默认false
     */
    boolean saveResult() default false;
}
