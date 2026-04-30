package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户消费记录DTO
 *
 * @author 刘建国
 */
@Data
@Schema(description = "客户消费记录数据传输对象")
public class CustomerOrderRecordDTO {

    @Schema(description = "客户ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "客户ID不能为空")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "陪玩师ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long companionId;

    @Schema(description = "订单类型", example = "陪玩", allowableValues = {"陪玩", "语音", "视频"})
    @NotNull(message = "订单类型不能为空")
    private String orderType;

    @Schema(description = "下单时间", example = "2026-01-01T10:00:00")
    @NotNull(message = "下单时间不能为空")
    private LocalDateTime orderTime;

    @Schema(description = "服务时长(小时)", example = "2.0")
    private BigDecimal durationHours;

    @Schema(description = "消费金额", example = "176.00")
    @NotNull(message = "消费金额不能为空")
    private BigDecimal amount;

    @Schema(description = "游戏类型", example = "王者荣耀")
    private String gameType;

    @Schema(description = "陪玩师等级", example = "GOLD", allowableValues = {"BRONZE", "SILVER", "GOLD", "DIAMOND", "STAR"})
    private String companionLevel;

    @Schema(description = "时间段", example = "10:00-12:00")
    private String timeSlot;

    @Schema(description = "评分", example = "5")
    private Integer rating;

    @Schema(description = "评价内容", example = "服务很好，陪玩师很专业")
    private String reviewContent;

    @Schema(description = "状态", example = "COMPLETED", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private String status;

    @Schema(description = "备注", example = "用户要求换陪玩师")
    private String remark;
}
