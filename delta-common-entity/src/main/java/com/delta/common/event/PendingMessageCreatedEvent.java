package com.delta.common.event;

import com.delta.common.vo.NotificationVO;
import org.springframework.context.ApplicationEvent;

/**
 * 待处理消息创建事件，用于通知客服有新的待处理工单
 *
 * @author 刘建国
 */
public class PendingMessageCreatedEvent extends ApplicationEvent {

    private final NotificationVO notification;

    public PendingMessageCreatedEvent(Object source, NotificationVO notification) {
        super(source);
        this.notification = notification;
    }

    public NotificationVO getNotification() {
        return notification;
    }
}
