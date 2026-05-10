package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "服务跟踪视图对象")
public class ServiceTrackVO extends BaseVO {

    @Schema(description = "跟踪ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "工单ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long workOrderId;

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "跟踪状态", example = "BOOKED", allowableValues = {"CONSULTING", "BOOKED", "IN_SERVICE", "COMPLETED", "CONFIRMED"})
    private String trackStatus;

    @Schema(description = "跟踪状态描述", example = "已预约")
    private String trackStatusDesc;

    @Schema(description = "咨询开始时间", example = "2026-01-01 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consultStartedAt;

    @Schema(description = "咨询内容", example = "用户咨询陪玩服务详情")
    private String consultContent;

    @Schema(description = "预约时间", example = "2026-01-01 09:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookedAt;

    @Schema(description = "预约陪玩师ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long bookedCompanionId;

    @Schema(description = "预约陪玩师名称", example = "小明同学")
    private String bookedCompanionName;

    @Schema(description = "预约服务类型", example = "陪玩")
    private String bookedServiceType;

    @Schema(description = "预约时间段", example = "10:00-12:00")
    private String bookedTimeSlot;

    @Schema(description = "关联订单ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long relatedOrderId;

    @Schema(description = "服务开始时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceStartedAt;

    @Schema(description = "服务陪玩师ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long serviceCompanionId;

    @Schema(description = "服务陪玩师名称", example = "小明同学")
    private String serviceCompanionName;

    @Schema(description = "服务时长(分钟)", example = "120")
    private Integer serviceDuration;

    @Schema(description = "服务结束时间", example = "2026-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceEndedAt;

    @Schema(description = "服务结果", example = "已完成")
    private String serviceResult;

    @Schema(description = "确认时间", example = "2026-01-01 12:05:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedAt;

    @Schema(description = "客户评分", example = "5")
    private Integer customerRating;

    @Schema(description = "客户反馈", example = "服务很好，非常满意")
    private String customerFeedback;
}
