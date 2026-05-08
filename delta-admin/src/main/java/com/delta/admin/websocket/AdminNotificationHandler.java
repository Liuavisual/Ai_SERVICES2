package com.delta.admin.websocket;

import com.delta.common.vo.NotificationVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理员通知WebSocket处理器，实时推送待处理工单通知
 *
 * @author 刘建国
 */
@Component
public class AdminNotificationHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationHandler.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("管理员连接成功，当前在线人数: {}", sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("管理员连接断开，当前在线人数: {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到管理员消息: {}", message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
        sessions.remove(session);
    }

    public void sendNotification(NotificationVO notification) {
        try {
            String json = objectMapper.writeValueAsString(notification);
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                        log.info("已向管理员发送通知: {}", notification);
                    } catch (IOException e) {
                        log.error("发送通知失败", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("序列化通知失败", e);
        }
    }
}
