package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 待处理消息实体
 * <p>
 * 对应数据库表 pending_messages，记录需要人工客服介入的待处理消息，
 * 包括分配状态、截止时间、升级级别、上下文摘要等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pending_messages")
@Table(name = "pending_messages", indexes = {
        @Index(name = "idx_pending_messages_user_id", columnList = "user_id"),
        @Index(name = "idx_pending_messages_status", columnList = "status"),
        @Index(name = "idx_pending_messages_assigned_cs_user_id", columnList = "assigned_cs_user_id"),
        @Index(name = "idx_pending_messages_deadline", columnList = "deadline"),
        @Index(name = "idx_pending_messages_escalation_level", columnList = "escalation_level"),
        @Index(name = "idx_pending_messages_status_deadline", columnList = "status,deadline")
})
public class PendingMessage extends BaseEntity {

    /** 关联的消息ID */
    private Long messageId;

    /** 关联的客户ID */
    private Long userId;

    /** 来源平台 */
    private String platform;

    /** 触发关键词 */
    private String keyword;

    /** 介入类型 */
    private String interventionType;

    /** 处理状态 */
    @TableField("status")
    private String status;

    /** 处理截止时间 */
    @TableField("deadline")
    private LocalDateTime deadline;

    /** 升级级别 */
    @TableField("escalation_level")
    private Integer escalationLevel;

    /** 分配的客服ID */
    @TableField("assigned_cs_user_id")
    private Long assignedCsUserId;

    /** 提醒次数 */
    @TableField("reminder_count")
    private Integer reminderCount;

    /** 处理人ID */
    private Long handledBy;

    /** 处理时间 */
    private LocalDateTime handledAt;

    /** 备注 */
    private String remark;

    /** 上下文摘要 */
    @TableField(value = "context_summary", jdbcType = JdbcType.LONGVARCHAR)
    private String contextSummary;
}
