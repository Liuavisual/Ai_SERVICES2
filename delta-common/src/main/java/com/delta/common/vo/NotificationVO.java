package com.delta.common.vo;

import lombok.Data;

/**
 * 通知视图对象
 *
 * @author delta
 */
@Data
public class NotificationVO {

    /** 通知类型 */    private String type;
    /** 关联的待处理消息ID */    private Long pendingMessageId;
    /** 客户ID */    private Long userId;
    /** 客户昵称 */    private String userNickname;
    /** 触发关键词 */    private String keyword;
    /** 介入类型 */    private String interventionType;
    /** 来源平台 */    private String platform;
    /** 消息内容 */    private String messageContent;
    /** 截止时间 */    private String deadline;
    /** 对话上下文摘要 */    private String contextSummary;
    /** 时间戳 */    private Long timestamp;
}
