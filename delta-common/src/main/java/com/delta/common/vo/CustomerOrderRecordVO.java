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
@Schema(description = "客户消费记录视图对象")
public class CustomerOrderRecordVO extends BaseVO {

    @Schema(description = "记录ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "用户昵称", example = "小明")
    private String userNickname;

    @Schema(description = "陪玩师ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long companionId;

    @Schema(description = "陪玩师名称", example = "小明同学")
    private String companionName;

    @Schema(description = "订单类型", example = "陪玩", allowableValues = {"陪玩", "语音", "视频"})
    private String orderType;

    @Schema(description = "下单时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    @Schema(description = "服务时长(小时)", example = "2.0")
    private BigDecimal durationHours;

    @Schema(description = "消费金额", example = "176.00")
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

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
