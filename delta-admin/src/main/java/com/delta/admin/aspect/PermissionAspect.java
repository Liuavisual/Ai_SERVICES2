package com.delta.admin.aspect;

import com.delta.common.annotation.PermAuth;
import com.delta.common.exception.BusinessException;
import com.delta.common.service.PermissionService;
import com.delta.common.util.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 权限校验切面
 * <p>
 * 拦截带有 @PermAuth 注解的方法，从SecurityContext获取当前用户权限列表进行校验。
 * Order值设为LowestPrecedence-100，确保在事务切面之后执行。
 * </p>
 *
 * @author 刘建国
 */
@Aspect
@Component
@Order(org.springframework.core.Ordered.LOWEST_PRECEDENCE - 100)
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    private final PermissionService permissionService;

    public PermissionAspect(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Around("@annotation(com.delta.common.annotation.PermAuth) || @within(com.delta.common.annotation.PermAuth)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        PermAuth permAuth = getPermAuthAnnotation(joinPoint);
        if (permAuth == null) {
            return joinPoint.proceed();
        }

        String[] requiredPerms = permAuth.value();
        if (requiredPerms.length == 0 || (requiredPerms.length == 1 && "*".equals(requiredPerms[0]))) {
            return joinPoint.proceed();
        }

        List<String> userPerms = permissionService.getUserPermissions(userId);
        log.debug("权限校验: userId={}, required={}, userPerms={}", userId, requiredPerms, userPerms);

        boolean hasAccess = checkAccess(userPerms, requiredPerms, permAuth.mode());
        if (!hasAccess) {
            String requiredStr = String.join(",", requiredPerms);
            log.warn("权限不足: userId={}, required={}, userPerms={}", userId, requiredStr, userPerms);
            throw new BusinessException(403, "权限不足，无法执行该操作");
        }

        return joinPoint.proceed();
    }

    private PermAuth getPermAuthAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        PermAuth methodAnnotation = method.getAnnotation(PermAuth.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        return method.getDeclaringClass().getAnnotation(PermAuth.class);
    }

    private boolean checkAccess(List<String> userPerms, String[] requiredPerms, PermAuth.MatchMode mode) {
        if (mode == PermAuth.MatchMode.ALL) {
            for (String required : requiredPerms) {
                if (!userPerms.contains(required)) {
                    return false;
                }
            }
            return true;
        }

        for (String required : requiredPerms) {
            if (userPerms.contains(required)) {
                return true;
            }
        }
        return false;
    }
}