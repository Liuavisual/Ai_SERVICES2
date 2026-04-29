package com.delta.admin.config;

import com.delta.admin.websocket.AdminNotificationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AdminNotificationHandler notificationHandler;

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Value("${websocket.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:8080}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationHandler, "/v1/ws/notify")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins(allowedOrigins.split(","));
    }
}
