package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单视图对象")
public class OrderVO extends BaseVO {

    @Schema(description = "订单ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "订单编号", example = "ORD20260101000001")
    private String orderNo;

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "陪玩师ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long companionId;

    @Schema(description = "陪玩师名称", example = "小明同学")
    private String companionName;

    @Schema(description = "陪玩师头像", example = "https://example.com/avatar/001.jpg")
    private String companionAvatar;

    @Schema(description = "服务类型", example = "陪玩", allowableValues = {"陪玩", "语音", "视频"})
    private String serviceType;

    @Schema(description = "订单状态", example = "COMPLETED", allowableValues = {"PENDING", "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private String orderStatus;

    @Schema(description = "支付状态", example = "PAID", allowableValues = {"UNPAID", "PAID", "REFUNDED"})
    private String paymentStatus;

    @Schema(description = "预约开始时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStart;

    @Schema(description = "预约结束时间", example = "2026-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledEnd;

    @Schema(description = "实际开始时间", example = "2026-01-01 10:05:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStart;

    @Schema(description = "实际结束时间", example = "2026-01-01 12:10:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEnd;

    @Schema(description = "服务时长(分钟)", example = "125")
    private Integer durationMinutes;

    @Schema(description = "总金额", example = "176.00")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额", example = "168.00")
    private BigDecimal paidAmount;

    @Schema(description = "游戏类型", example = "王者荣耀")
    private String gameType;

    @Schema(description = "备注", example = "希望选择擅长打野的陪玩师")
    private String remark;

    @Schema(description = "时间选择方式", example = "SYSTEM", allowableValues = {"SYSTEM", "CUSTOM"})
    private String timeSource;

    @Schema(description = "取消/拒单原因", example = "时间冲突")
    private String cancelReason;

    @Schema(description = "关联排班记录ID")
    private Long scheduleId;

    @Schema(description = "来源", example = "微信", allowableValues = {"微信", "企微", "APP", "网页"})
    private String source;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 12:10:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "订单状态文本", example = "已完成")
    private String orderStatusText;

    @Schema(description = "支付状态文本", example = "已支付")
    private String paymentStatusText;
}
