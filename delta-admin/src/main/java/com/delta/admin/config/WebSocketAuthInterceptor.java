package com.delta.admin.config;

import com.delta.common.config.JwtConfig;
import com.delta.common.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtUtils jwtUtils;
    private final JwtConfig jwtConfig;

    public WebSocketAuthInterceptor(JwtUtils jwtUtils, JwtConfig jwtConfig) {
        this.jwtUtils = jwtUtils;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (!jwtConfig.isEnabled()) {
            return true;
        }

        String token = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            token = servletRequest.getServletRequest().getParameter("token");
        }

        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket连接缺少Token参数，拒绝连接: ip={}", request.getRemoteAddress());
            return false;
        }

        try {
            Claims claims = jwtUtils.parseToken(token);
            String tokenType = claims.get("type", String.class);
            if ("refresh".equals(tokenType)) {
                log.warn("WebSocket连接使用refreshToken，拒绝连接");
                return false;
            }

            if (jwtUtils.isTokenExpired(token)) {
                log.warn("WebSocket连接Token已过期，拒绝连接");
                return false;
            }

            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            attributes.put("userId", userId);
            attributes.put("username", username);
            attributes.put("role", role);

            log.info("WebSocket认证成功: userId={}, username={}, role={}", userId, username, role);
            return true;
        } catch (Exception e) {
            log.warn("WebSocket连接Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
