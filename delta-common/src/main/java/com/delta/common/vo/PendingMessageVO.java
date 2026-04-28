package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class PendingMessageVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long messageId;
    @ObfuscatedId
    private Long userId;
    private String userNickname;
    private String userPlatform;
    private String platform;
    private String keyword;
    private String interventionType;
    private String interventionTypeDesc;
    private String status;
    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer escalationLevel;
    @ObfuscatedId
    private Long assignedCsUserId;
    private String assignedCsUserName;
    private Integer reminderCount;
    @ObfuscatedId
    private Long handledBy;
    private String handledByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handledAt;

    private String remark;
    private String messageContent;
    private String contextSummary;

    public Long getRemainingSeconds() {
        if (deadline == null) {
            return null;
        }
        return java.time.Duration.between(LocalDateTime.now(), deadline).getSeconds();
    }

    public Boolean getOverdue() {
        Long remaining = getRemainingSeconds();
        return remaining != null && remaining < 0;
    }

    public Boolean getUrgent() {
        Long remaining = getRemainingSeconds();
        return remaining != null && remaining >= 0 && remaining < 300;
    }
}
