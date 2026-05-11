package com.delta.admin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单通知WebSocket处理器，实时推送订单状态变更通知
 *
 * @author 刘建国
 */
@Component
public class OrderNotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("订单通知连接成功，当前在线人数: {}", sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("订单通知连接断开，当前在线人数: {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到订单通知消息: {}", message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("订单通知WebSocket传输错误", exception);
        sessions.remove(session);
    }

    /**
     * 广播订单状态变更通知
     *
     * @param orderId      订单ID
     * @param orderNo      订单编号
     * @param fromStatus   变更前状态
     * @param toStatus     变更后状态
     * @param operatorName 操作人姓名
     * @param reason       变更原因
     */
    public void broadcastOrderStatusChange(Long orderId, String orderNo, String fromStatus,
                                            String toStatus, String operatorName, String reason) {
        Map<String, Object> notification = new ConcurrentHashMap<>();
        notification.put("type", "ORDER_STATUS_CHANGE");
        notification.put("orderId", orderId);
        notification.put("orderNo", orderNo);
        notification.put("fromStatus", fromStatus);
        notification.put("toStatus", toStatus);
        notification.put("operatorName", operatorName);
        notification.put("reason", reason);
        notification.put("timestamp", System.currentTimeMillis());

        sendToAll(notification);
    }

    /**
     * 广播新订单通知
     *
     * @param orderId       订单ID
     * @param orderNo       订单编号
     * @param companionName 陪玩师姓名
     */
    public void broadcastNewOrder(Long orderId, String orderNo, String companionName) {
        Map<String, Object> notification = new ConcurrentHashMap<>();
        notification.put("type", "NEW_ORDER");
        notification.put("orderId", orderId);
        notification.put("orderNo", orderNo);
        notification.put("companionName", companionName);
        notification.put("timestamp", System.currentTimeMillis());

        sendToAll(notification);
    }

    /**
     * 向所有连接的客户端发送消息
     *
     * @param payload 消息内容
     */
    private void sendToAll(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("发送订单通知失败", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("序列化订单通知失败", e);
        }
    }
}