package com.delta.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端IP地址工具类
 * <p>
 * 统一获取客户端真实IP地址的逻辑，支持代理、负载均衡等场景。
 * 所有模块统一使用此工具类，避免代码重复。
 * </p>
 *
 * @author 刘建国
 */
public final class ClientIpUtils {

    private ClientIpUtils() {
    }

    /**
     * 获取客户端真实IP地址
     * <p>
     * 优先级：X-Forwarded-For > X-Real-IP > RemoteAddr
     * </p>
     *
     * @param request HTTP请求
     * @return 客户端IP地址，不会返回null
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (isInvalid(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isInvalid(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    /**
     * 判断IP字符串是否无效
     *
     * @param ip IP字符串
     * @return 是否无效
     */
    private static boolean isInvalid(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }
}