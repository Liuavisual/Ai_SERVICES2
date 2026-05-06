package com.delta.common.aspect;

import com.delta.common.annotation.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计日志切面
 * <p>
 * 拦截所有标注了@AuditLog注解的方法，自动记录操作模块、动作、
 * 用户ID、客户端IP、请求方法、URI、执行状态、耗时等信息。
 * 日志输出到名为"AUDIT_LOG"的独立Logger，便于单独收集和归档。
 * </p>
 *
 * @author 刘建国
 */
@Aspect
@Component
public class AuditLogAspect {

    /**
     * 审计日志专用Logger，与业务日志分离
     */
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT_LOG");

    /**
     * 环绕通知，拦截@AuditLog注解标注的方法
     *
     * @param joinPoint 切点信息
     * @param auditLog  审计日志注解实例
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 默认用户名
        String username = "anonymous";
        // 默认IP
        String ip = "unknown";
        // 默认HTTP方法
        String method = "UNKNOWN";
        // 默认URI
        String uri = "UNKNOWN";

        // 从RequestContextHolder获取当前HTTP请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ip = request.getRemoteAddr();
            method = request.getMethod();
            uri = request.getRequestURI();
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr != null) {
                username = userIdAttr.toString();
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        // 从注解中获取模块和动作，若为空则使用类名和方法名
        String module = auditLog.module();
        String action = auditLog.action();
        if (module.isEmpty()) {
            module = className;
        }
        if (action.isEmpty()) {
            action = methodName;
        }

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
            String errorMsg = error != null ? error.getMessage() : "";

            StringBuilder logMsg = new StringBuilder();
            logMsg.append("[").append(module).append("] ");
            logMsg.append(action).append(" | ");
            logMsg.append("user=").append(username).append(" | ");
            logMsg.append("ip=").append(ip).append(" | ");
            logMsg.append("method=").append(method).append(" | ");
            logMsg.append("uri=").append(uri).append(" | ");
            logMsg.append("status=").append(status).append(" | ");
            logMsg.append("duration=").append(duration).append("ms");
            if (auditLog.saveResult() && result != null) {
                logMsg.append(" | result=").append(result);
            }
            if (!errorMsg.isEmpty()) {
                logMsg.append(" | error=").append(errorMsg);
            }

            // 根据状态选择日志级别
            if ("FAIL".equals(status)) {
                AuditLogAspect.auditLog.error(logMsg.toString());
            } else {
                AuditLogAspect.auditLog.info(logMsg.toString());
            }
        }
    }

    /**
     * 获取客户端真实IP地址
     * <p>
     * 优先从代理头X-Forwarded-For和X-Real-IP获取，
     * 最后回退到request.getRemoteAddr()。
     * </p>
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        // 尝试从X-Forwarded-For头获取IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 尝试从X-Real-IP头获取IP
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 使用远程地址作为备选
            ip = request.getRemoteAddr();
        }
        // 如果存在多个IP（经过多层代理），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
