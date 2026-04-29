package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知视图对象")
public class NotificationVO extends BaseVO {

    @Schema(description = "通知类型", example = "PENDING_MESSAGE", allowableValues = {"PENDING_MESSAGE", "ESCALATION", "REMINDER"})
    private String type;

    @Schema(description = "待处理消息ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long pendingMessageId;

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "用户昵称", example = "小明")
    private String userNickname;

    @Schema(description = "触发关键词", example = "退款")
    private String keyword;

    @Schema(description = "干预类型", example = "KEYWORD", allowableValues = {"KEYWORD", "EMOTION", "ORDER_INTENT", "MANUAL"})
    private String interventionType;

    @Schema(description = "来源平台", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    private String platform;

    @Schema(description = "消息内容", example = "我要退款")
    private String messageContent;

    @Schema(description = "截止时间", example = "2026-01-01 10:30:00")
    private String deadline;

    @Schema(description = "上下文摘要", example = "用户对上次服务不满意，要求退款")
    private String contextSummary;

    @Schema(description = "时间戳", example = "1704067200000")
    private Long timestamp;
}
