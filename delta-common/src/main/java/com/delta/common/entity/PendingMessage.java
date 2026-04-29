package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pending_messages")
public class PendingMessage extends BaseEntity {

    private Long messageId;

    private Long userId;

    private String platform;

    private String keyword;

    private String interventionType;

    @TableField("status")
    private String status;

    @TableField("deadline")
    private LocalDateTime deadline;

    @TableField("escalation_level")
    private Integer escalationLevel;

    @TableField("assigned_cs_user_id")
    private Long assignedCsUserId;

    @TableField("reminder_count")
    private Integer reminderCount;

    private Long handledBy;

    private LocalDateTime handledAt;

    private String remark;

    @TableField("context_summary")
    private String contextSummary;
}
