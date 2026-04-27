package com.delta.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待处理消息视图对象
 *
 * @author delta
 */
@Data
public class PendingMessageVO {

    /** 主键ID */    private Long id;
    /** 原始消息ID */    private Long messageId;

    /** 客户ID */    private Long userId;
    /** 客户昵称 */    private String userNickname;
    /** 客户平台 */    private String userPlatform;

    /** 来源平台 */    private String platform;
    /** 触发关键词 */    private String keyword;
    /** 介入类型编码 */    private String interventionType;
    /** 介入类型描述 */    private String interventionTypeDesc;

    /** 状态编码 */    private String status;
    /** 状态描述 */    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 截止时间 */    private LocalDateTime deadline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 升级级别 */    private Integer escalationLevel;
    /** 分配的客服ID */    private Long assignedCsUserId;
    /** 客服姓名 */    private String assignedCsUserName;
    /** 提醒次数 */    private Integer reminderCount;

    /** 处理人ID */    private Long handledBy;
    /** 处理人姓名 */    private String handledByName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 处理时间 */    private LocalDateTime handledAt;

    /** 处理备注 */    private String remark;

    /** 消息内容 */    private String messageContent;

    /** 对话上下文摘要 */    private String contextSummary;

    public long getRemainingSeconds() {
        if (deadline == null) return 0;
        if (!"pending".equals(status) && !"processing".equals(status)) return 0;
        return java.time.Duration.between(LocalDateTime.now(), deadline).getSeconds();
    }

    public boolean isOverdue() {
        return getRemainingSeconds() < 0;
    }

    public boolean isUrgent() {
        if (deadline == null) return false;
        return !isOverdue() && getRemainingSeconds() < 120;
    }
}
