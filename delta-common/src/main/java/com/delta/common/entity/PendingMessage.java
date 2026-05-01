package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pending_messages")
public class PendingMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String platform;

    @TableField(value = "content", jdbcType = JdbcType.LONGVARCHAR)
    private String content;

    private String contentType;

    private String pendingReason;

    private String priority;

    @TableField(exist = false)
    private Long messageId;

    @TableField(exist = false)
    private String keyword;

    @TableField(exist = false)
    private String interventionType;

    @TableField(exist = false)
    private String status;

    @TableField(exist = false)
    private LocalDateTime deadline;

    @TableField(exist = false)
    private Integer escalationLevel;

    @TableField(exist = false)
    private Long assignedCsUserId;

    @TableField(exist = false)
    private Integer reminderCount;

    @TableField(exist = false)
    private Long handledBy;

    @TableField(exist = false)
    private LocalDateTime handledAt;

    @TableField(exist = false)
    private String remark;

    @TableField(exist = false)
    private String contextSummary;
}
