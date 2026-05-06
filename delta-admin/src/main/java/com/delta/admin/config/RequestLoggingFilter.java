package com.delta.admin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 请求日志过滤器
 * 记录每个HTTP请求的路径、耗时、响应状态码，用于生产环境监控和问题排查
 *
 * @author 刘建国
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final int SLOW_REQUEST_THRESHOLD_MS = 3000;

    private static final String[] EXCLUDED_PATHS = {
        "/actuator", "/druid", "/doc.html", "/webjars", "/swagger-resources", "/v3/api-docs"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        for (String excluded : EXCLUDED_PATHS) {
            if (uri.startsWith(excluded)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String clientIp = getClientIp(request);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 0);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            int statusCode = responseWrapper.getStatus();

            if (elapsed > SLOW_REQUEST_THRESHOLD_MS) {
                log.warn("慢请求 {} {} {} {}ms IP={}", method, uri, statusCode, elapsed, clientIp);
            } else if (statusCode >= 400) {
                log.warn("请求异常 {} {} {} {}ms IP={}", method, uri, statusCode, elapsed, clientIp);
            } else {
                log.debug("请求 {} {} {} {}ms IP={}", method, uri, statusCode, elapsed, clientIp);
            }

            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
