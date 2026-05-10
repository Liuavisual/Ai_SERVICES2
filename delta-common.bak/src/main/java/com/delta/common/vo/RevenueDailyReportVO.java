package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "营收日报视图对象")
public class RevenueDailyReportVO extends BaseVO {

    @Schema(description = "报表ID")
    @ObfuscatedId
    private Long id;

    @Schema(description = "俱乐部配置ID")
    @ObfuscatedId
    private Long clubConfigId;

    @Schema(description = "俱乐部名称")
    private String clubName;

    @Schema(description = "报表日期")
    private LocalDate reportDate;

    @Schema(description = "游戏类型")
    private String gameType;

    @Schema(description = "订单总数")
    private Integer totalOrders;

    @Schema(description = "已完成订单数")
    private Integer completedOrders;

    @Schema(description = "退款订单数")
    private Integer refundOrders;

    @Schema(description = "订单总收入")
    private BigDecimal totalRevenue;

    @Schema(description = "平台分成收入")
    private BigDecimal platformIncome;

    @Schema(description = "AI会话总数")
    private Integer aiConversations;

    @Schema(description = "AI处理率(%)")
    private BigDecimal aiHandleRate;

    @Schema(description = "客户满意度均分")
    private BigDecimal avgSatisfaction;

    @Schema(description = "新客户数")
    private Integer newCustomers;

    @Schema(description = "老客户复购数")
    private Integer repeatCustomers;

    @Schema(description = "活跃陪玩师数")
    private Integer activeCompanions;
}
