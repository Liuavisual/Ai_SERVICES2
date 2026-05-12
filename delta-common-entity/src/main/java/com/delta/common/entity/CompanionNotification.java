package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 陪玩师通知消息实体
 *
 * @author 刘建国
 */
@Data
@TableName("companion_notifications")
public class CompanionNotification {

    /** 主键ID */
    private Long id;

    /** 陪玩师ID */
    private Long companionId;

    /** 关联订单ID */
    private Long orderId;

    /** 通知类型:NEW_ORDER-新订单,STATUS_CHANGE-状态变更 */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 是否已读:0-未读,1-已读 */
    private Integer isRead;

    /** 创建时间 */
    private LocalDateTime createdAt;
}