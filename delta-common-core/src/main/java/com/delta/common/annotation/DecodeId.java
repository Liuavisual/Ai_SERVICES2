package com.delta.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ID自动解码注解
 * 用于标记Controller方法中需要自动解码的ID参数（路径变量或请求参数）
 *
 * <pre>
 * &#64;GetMapping("/{id}")
 * public Result&lt;SomeVO&gt; getById(@PathVariable("id") @DecodeId Long id) {
 *     return Result.success(someService.getById(id));
 * }
 * </pre>
 *
 * @author 刘建国
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DecodeId {

    /**
     * 是否允许为空
     * 默认true，当参数为null或空字符串时返回null
     * 设为false时，参数为null或空字符串将抛出异常
     */
    boolean required() default true;
}