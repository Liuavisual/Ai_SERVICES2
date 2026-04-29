package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "待处理消息视图对象")
public class PendingMessageVO extends BaseVO {

    @Schema(description = "待处理消息ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "关联消息ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long messageId;

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "用户昵称", example = "小明")
    private String userNickname;

    @Schema(description = "用户平台", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    private String userPlatform;

    @Schema(description = "平台标识", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    private String platform;

    @Schema(description = "触发关键词", example = "退款")
    private String keyword;

    @Schema(description = "干预类型", example = "KEYWORD", allowableValues = {"KEYWORD", "EMOTION", "ORDER_INTENT", "MANUAL"})
    private String interventionType;

    @Schema(description = "干预类型描述", example = "关键词触发")
    private String interventionTypeDesc;

    @Schema(description = "状态", example = "PENDING", allowableValues = {"PENDING", "HANDLING", "HANDLED", "ESCALATED", "IGNORED"})
    private String status;

    @Schema(description = "状态描述", example = "待处理")
    private String statusDesc;

    @Schema(description = "处理截止时间", example = "2026-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "升级级别", example = "1")
    private Integer escalationLevel;

    @Schema(description = "分配的客服ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long assignedCsUserId;

    @Schema(description = "分配的客服名称", example = "客服小李")
    private String assignedCsUserName;

    @Schema(description = "提醒次数", example = "2")
    private Integer reminderCount;

    @Schema(description = "处理人ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long handledBy;

    @Schema(description = "处理人名称", example = "客服小王")
    private String handledByName;

    @Schema(description = "处理时间", example = "2026-01-01 10:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handledAt;

    @Schema(description = "处理备注", example = "已联系用户解决问题")
    private String remark;

    @Schema(description = "消息内容", example = "我要退款")
    private String messageContent;

    @Schema(description = "上下文摘要", example = "用户对上次服务不满意，要求退款")
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
