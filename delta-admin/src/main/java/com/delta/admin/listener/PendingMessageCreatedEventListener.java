package com.delta.admin.listener;

import com.delta.admin.websocket.AdminNotificationHandler;
import com.delta.common.event.PendingMessageCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 待处理消息创建事件监听器，通过WebSocket通知在线客服
 *
 * @author delta
 */
@Component
public class PendingMessageCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(PendingMessageCreatedEventListener.class);

    @Autowired
    private AdminNotificationHandler notificationHandler;

    @EventListener
    public void handlePendingMessageCreatedEvent(PendingMessageCreatedEvent event) {
        log.info("收到待处理消息创建事件: {}", event.getNotification());
        notificationHandler.sendNotification(event.getNotification());
    }
}
