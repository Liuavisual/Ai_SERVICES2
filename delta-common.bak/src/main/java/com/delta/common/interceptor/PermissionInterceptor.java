package com.delta.common.interceptor;

import com.delta.common.annotation.RequirePermission;
import com.delta.common.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限校验拦截器
 * <p>
 * 在请求到达Controller方法前，检查 @RequirePermission 注解声明的权限。
 * 权限规则：
 * <ol>
 *   <li>SYS_ADMIN 角色拥有所有权限，直接放行</li>
 *   <li>CS_LEADER 角色拥有 view/edit 类权限，通过相关检查放行</li>
 *   <li>CS_STAFF 角色需要精确匹配分配的权限</li>
 * </ol>
 * </p>
 *
 * @author 刘建国
 * @deprecated 请使用 Spring Security 的 @PreAuthorize 注解替代，权限体系已统一
 */
@Deprecated
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PermissionInterceptor.class);

    private final PermissionService permissionService;

    @Override
    @Deprecated
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (annotation == null || annotation.value().length == 0) {
            return true;
        }

        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未认证\"}");
            return false;
        }

        Long userId = (Long) userIdAttr;
        String role = (String) request.getAttribute("role");

        // SYS_ADMIN 拥有所有权限
        if ("SYS_ADMIN".equals(role)) {
            return true;
        }

        // CS_LEADER 拥有管理权限（view/edit/manage）
        if ("CS_LEADER".equals(role)) {
            for (String required : annotation.value()) {
                if (required.startsWith("config:") || required.startsWith("system:")) {
                    if (!permissionService.hasPermission(userId, required)) {
                        log.warn("CS_LEADER权限不足: userId={}, required={}", userId, required);
                        response.setStatus(403);
                        response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                        return false;
                    }
                }
            }
            return true;
        }

        for (String required : annotation.value()) {
            if (!permissionService.hasPermission(userId, required)) {
                log.warn("权限不足: userId={}, required={}", userId, required);
                response.setStatus(403);
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                return false;
            }
        }

        return true;
    }
}
