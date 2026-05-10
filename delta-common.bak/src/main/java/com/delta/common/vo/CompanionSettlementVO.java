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
@Schema(description = "陪玩师结算视图对象")
public class CompanionSettlementVO extends BaseVO {

    @Schema(description = "结算记录ID")
    @ObfuscatedId
    private Long id;

    @Schema(description = "陪玩师ID")
    @ObfuscatedId
    private Long companionId;

    @Schema(description = "陪玩师昵称")
    private String companionNickname;

    @Schema(description = "结算周期")
    private String settlementPeriod;

    @Schema(description = "接单总数")
    private Integer totalOrders;

    @Schema(description = "订单总收入")
    private BigDecimal totalRevenue;

    @Schema(description = "平台分成金额")
    private BigDecimal platformFee;

    @Schema(description = "陪玩师实得金额")
    private BigDecimal companionIncome;

    @Schema(description = "扣款项")
    private BigDecimal deductionAmount;

    @Schema(description = "扣款原因")
    private String deductionReason;

    @Schema(description = "结算状态")
    private String settlementStatus;

    @Schema(description = "实际结算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settledAt;

    @Schema(description = "收款方式")
    private String paymentMethod;

    @Schema(description = "收款账号")
    private String paymentAccount;

    @Schema(description = "陪玩师确认状态")
    private String confirmStatus;

    @Schema(description = "申诉内容")
    private String disputeContent;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
