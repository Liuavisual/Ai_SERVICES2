package com.delta.admin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端实时聊天WebSocket处理器
 * 支持用户与客服之间的实时消息推送
 *
 * @author 刘建国
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /** 用户ID → WebSocket会话映射 */
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            log.info("【WebSocket】客户端连接建立, userId: {}, sessionId: {}", userId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = extractUserId(session);
        String payload = message.getPayload();
        log.debug("【WebSocket】收到客户端消息, userId: {}, content: {}", userId, payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("【WebSocket】客户端连接断开, userId: {}, status: {}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("【WebSocket】传输错误, sessionId: {}", session.getId(), exception);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException ignored) {
        }
    }

    /**
     * 向指定用户推送消息
     *
     * @param userId  目标用户ID
     * @param message 消息内容（Map会被序列化为JSON）
     */
    public void sendToUser(Long userId, Object message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = message instanceof String ? (String) message : objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.debug("【WebSocket】推送消息成功, userId: {}", userId);
            } catch (IOException e) {
                log.warn("【WebSocket】推送消息失败, userId: {}", userId, e);
                USER_SESSIONS.remove(userId);
            }
        }
    }

    /**
     * 向所有在线用户广播消息
     *
     * @param message 广播消息
     */
    public void broadcast(Object message) {
        USER_SESSIONS.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    String json = message instanceof String ? (String) message : objectMapper.writeValueAsString(message);
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.warn("【WebSocket】广播消息失败, userId: {}", userId);
                }
            }
        });
    }

    /**
     * 获取当前在线用户数
     *
     * @return 在线用户数
     */
    public int getOnlineCount() {
        return USER_SESSIONS.size();
    }

    /**
     * 从WebSocket会话中提取用户ID
     *
     * @param session WebSocket会话
     * @return 用户ID，提取失败返回null
     */
    private Long extractUserId(WebSocketSession session) {
        if (session.getUri() == null) return null;
        String query = session.getUri().getQuery();
        if (query == null) return null;
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("userId=")) {
                try {
                    return Long.parseLong(param.substring(7));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}