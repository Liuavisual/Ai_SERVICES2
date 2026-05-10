package com.delta.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 核心逻辑保护注解
 * <p>
 * 用于标记包含核心竞争壁垒逻辑的方法。
 * 配合 {@link com.delta.common.aspect.ProtectionAspect} 切面实现：
 * <ol>
 *   <li>生产环境关闭详细参数和返回值日志</li>
 *   <li>运行时校验调用来源，防止反射/反编译工具直接调用</li>
 *   <li>关键算法逻辑的代码保护</li>
 * </ol>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;ProtectedLogic(level = ProtectionLevel.CORE)
 * public String processIntent(String message) {
 *     // 核心意图识别逻辑
 * }
 * </pre>
 * </p>
 *
 * @author 刘建国
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtectedLogic {

    /**
     * 保护级别
     * <ul>
     *   <li>CORE - 核心竞争方法，最严格保护</li>
     *   <li>HIGH - 重要业务方法</li>
     *   <li>MEDIUM - 中等保护级别</li>
     * </ul>
     *
     * @return 保护级别
     */
    ProtectionLevel level() default ProtectionLevel.CORE;

    /**
     * 方法描述（用于审计和内部文档）
     *
     * @return 描述信息
     */
    String description() default "";

    /**
     * 是否允许内部反射调用（测试和内部工具使用）
     * <p>
     * 生产环境默认关闭，仅开发/测试环境可开启。
     * </p>
     *
     * @return true表示允许反射调用
     */
    boolean allowReflection() default false;

    /**
     * 保护级别枚举
     */
    enum ProtectionLevel {
        /** 核心竞争方法 - 最严格保护，生产环境零日志 */
        CORE,
        /** 重要业务方法 - 仅记录方法名和耗时 */
        HIGH,
        /** 中等保护 - 记录方法名，不记录参数 */
        MEDIUM
    }
}
