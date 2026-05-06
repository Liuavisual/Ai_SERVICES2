package com.delta.common.aspect;

import com.delta.common.annotation.ProtectedLogic;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * 核心逻辑保护切面
 * <p>
 * 拦截所有标注了 @{@link ProtectedLogic} 注解的方法，实现以下保护策略：
 * <ol>
 *   <li><b>日志保护</b>：生产环境下CORE级别方法关闭详细参数和返回值日志，
 *       仅记录方法名和耗时，防止关键算法逻辑通过日志泄露。</li>
 *   <li><b>调用来源校验</b>：运行时检测调用栈，拦截来自反射(Reflection)、
 *       JNI、反编译工具（如JD-GUI、Luyten）的非法直接调用。</li>
 *   <li><b>性能监控</b>：记录核心方法的执行耗时，用于性能分析和异常检测。</li>
 * </ol>
 * </p>
 * <p>
 * 保护级别说明：
 * <ul>
 *   <li>CORE - 零日志模式，不记录任何参数和返回值</li>
 *   <li>HIGH - 仅记录方法签名和耗时</li>
 *   <li>MEDIUM - 记录方法签名，不记录参数详情</li>
 * </ul>
 * </p>
 *
 * @author 刘建国
 */
@Aspect
@Component
public class ProtectionAspect {

    /** 保护切面专用Logger */
    private static final Logger log = LoggerFactory.getLogger(ProtectionAspect.class);

    /** 当前运行环境，从配置文件 spring.profiles.active 读取 */
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /** 反射API特征类名列表，用于检测非法调用来源 */
    private static final String[] REFLECTION_SIGNATURES = {
            "java.lang.reflect.Method",
            "jdk.internal.reflect",
            "sun.reflect",
            "net.sf.cglib",
            "org.springframework.cglib",
            "com.intellij.rt",
            "org.jetbrains",
            "jd.ide",
            "luyten",
            "decompiler"
    };

    /**
     * 环绕通知，拦截@ProtectedLogic注解标注的方法
     * <p>
     * 核心流程：
     * 1. 校验调用来源（防止反射/反编译工具直接调用）
     * 2. 根据保护级别决定日志输出策略
     * 3. 执行目标方法并记录耗时
     * </p>
     *
     * @param joinPoint      切点信息
     * @param protectedLogic 保护逻辑注解实例
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(protectedLogic)")
    public Object around(ProceedingJoinPoint joinPoint, ProtectedLogic protectedLogic) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        String fullMethodName = className + "." + methodName;

        // 第一步：校验调用来源合法性
        if (!isValidCaller(joinPoint, protectedLogic)) {
            log.error("【保护切面】检测到非法调用来源 | 方法={} | 调用者可能来自反射/反编译工具", fullMethodName);
            throw new SecurityException("核心竞争方法不允许通过反射或外部工具直接调用: " + fullMethodName);
        }

        // 第二步：根据保护级别决定日志策略
        ProtectedLogic.ProtectionLevel level = protectedLogic.level();
        boolean isProduction = isProductionProfile();

        // 第三步：记录方法入口（生产环境CORE级别不记录任何参数信息）
        logMethodEntry(fullMethodName, level, isProduction, joinPoint.getArgs());

        // 第四步：执行目标方法并记录耗时
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String status = error == null ? "SUCCESS" : "FAIL";

            // 记录方法出口（生产环境CORE级别不记录返回值）
            logMethodExit(fullMethodName, level, isProduction, status, duration, error, result);
        }
    }

    /**
     * 判断当前是否为生产环境
     *
     * @return true表示生产环境
     */
    private boolean isProductionProfile() {
        return "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
    }

    /**
     * 校验方法调用来源是否合法
     * <p>
     * 检测当前调用栈中是否包含反射API、反编译工具等非法调用源的特征类名。
     * 如果发现非法调用源且注解未显式允许反射调用，则拒绝执行。
     * </p>
     *
     * @param joinPoint      切点信息
     * @param protectedLogic 保护逻辑注解
     * @return true表示合法调用，false表示非法调用
     */
    private boolean isValidCaller(ProceedingJoinPoint joinPoint, ProtectedLogic protectedLogic) {
        // 生产环境才进行严格校验
        if (!isProductionProfile()) {
            return true;
        }

        // 如果注解允许反射调用，直接放行
        if (protectedLogic.allowReflection()) {
            return true;
        }

        // 检测调用栈中是否存在非法调用源
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            for (String signature : REFLECTION_SIGNATURES) {
                if (className.contains(signature)) {
                    log.warn("【保护切面】调用栈中发现可疑来源 | 类={} | 方法={} | 行={}",
                            className, element.getMethodName(), element.getLineNumber());
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 根据保护级别记录方法入口日志
     *
     * @param fullMethodName 完整方法名
     * @param level          保护级别
     * @param isProduction   是否生产环境
     * @param args           方法参数
     */
    private void logMethodEntry(String fullMethodName, ProtectedLogic.ProtectionLevel level,
                                 boolean isProduction, Object[] args) {
        switch (level) {
            case CORE:
                // CORE级别：生产环境零日志，开发环境仅记录方法签名
                if (!isProduction) {
                    log.debug("【核心方法】进入: {}", fullMethodName);
                }
                break;
            case HIGH:
                // HIGH级别：记录方法签名
                log.debug("【重要方法】进入: {}", fullMethodName);
                break;
            case MEDIUM:
                // MEDIUM级别：记录方法签名（不记录参数内容）
                log.debug("【保护方法】进入: {} | 参数个数={}", fullMethodName, args != null ? args.length : 0);
                break;
            default:
                break;
        }
    }

    /**
     * 根据保护级别记录方法出口日志
     *
     * @param fullMethodName 完整方法名
     * @param level          保护级别
     * @param isProduction   是否生产环境
     * @param status         执行状态
     * @param duration       执行耗时（毫秒）
     * @param error          异常对象
     * @param result         返回值
     */
    private void logMethodExit(String fullMethodName, ProtectedLogic.ProtectionLevel level,
                                boolean isProduction, String status, long duration,
                                Throwable error, Object result) {
        switch (level) {
            case CORE:
                if (isProduction) {
                    // 生产环境CORE级别：仅记录耗时和状态，不记录任何数据
                    if ("FAIL".equals(status)) {
                        log.error("【核心方法】执行异常 | 方法={} | 耗时={}ms | 异常类型={}",
                                fullMethodName, duration,
                                error != null ? error.getClass().getSimpleName() : "unknown");
                    }
                    // 成功时不记录日志（完全静默）
                } else {
                    // 开发环境：记录基本信息用于调试
                    log.debug("【核心方法】退出: {} | status={} | duration={}ms",
                            fullMethodName, status, duration);
                    if (error != null) {
                        log.error("【核心方法】异常详情 | 方法={} | error={}", fullMethodName, error.getMessage());
                    }
                }
                break;
            case HIGH:
                log.debug("【重要方法】退出: {} | status={} | duration={}ms",
                        fullMethodName, status, duration);
                if (error != null) {
                    log.error("【重要方法】异常 | 方法={} | error={}", fullMethodName, error.getMessage());
                }
                break;
            case MEDIUM:
                // MEDIUM级别：记录方法签名和耗时，不记录返回值
                log.debug("【保护方法】退出: {} | status={} | duration={}ms",
                        fullMethodName, status, duration);
                if (error != null) {
                    log.error("【保护方法】异常 | 方法={} | error={}", fullMethodName, error.getMessage());
                }
                break;
            default:
                break;
        }
    }
}
