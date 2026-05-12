package com.delta.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * 安全上下文工具类
 * <p>
 * 提供从SecurityContext获取当前登录用户信息的方法
 * </p>
 *
 * @author 刘建国
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名，未登录返回null
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    /**
     * 获取当前用户权限列表(从认证令牌中)
     *
     * @return 权限编码列表
     */
    @SuppressWarnings("unchecked")
    public static List<String> getCurrentUserPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getDetails() == null) {
            return List.of();
        }
        Object details = auth.getDetails();
        if (details instanceof List) {
            return (List<String>) details;
        }
        return List.of();
    }
}