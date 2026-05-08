package com.delta.admin.listener;

import com.delta.admin.websocket.AdminNotificationHandler;
import com.delta.common.event.PendingMessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 待处理消息创建事件监听器，通过WebSocket通知在线客服
 *
 * @author 刘建国
 */
@Component
@RequiredArgsConstructor
public class PendingMessageCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(PendingMessageCreatedEventListener.class);

    private final AdminNotificationHandler notificationHandler;

    @EventListener
    public void handlePendingMessageCreatedEvent(PendingMessageCreatedEvent event) {
        log.info("收到待处理消息创建事件: {}", event.getNotification());
        notificationHandler.sendNotification(event.getNotification());
    }
}
