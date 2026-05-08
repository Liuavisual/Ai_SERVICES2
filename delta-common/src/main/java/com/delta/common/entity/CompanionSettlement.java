package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 陪玩师结算记录实体
 * <p>
 * 对应数据库表 companion_settlement，记录陪玩师的收益和结算状态。
 * 支持陪玩师查询自己的收益明细（报告要求的双向服务能力）。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("companion_settlement")
@Table(name = "companion_settlement", indexes = {
        @Index(name = "idx_cst_companion_id", columnList = "companion_id"),
        @Index(name = "idx_cst_status", columnList = "settlement_status"),
        @Index(name = "idx_cst_period", columnList = "settlement_period")
})
public class CompanionSettlement extends BaseEntity {

    /** 陪玩师ID */
    @TableField("companion_id")
    private Long companionId;

    /** 结算周期（如 2026-05-01 至 2026-05-31） */
    @TableField("settlement_period")
    private String settlementPeriod;

    /** 接单总数 */
    @TableField("total_orders")
    private Integer totalOrders;

    /** 订单总收入 */
    @TableField("total_revenue")
    private BigDecimal totalRevenue;

    /** 平台分成金额 */
    @TableField("platform_fee")
    private BigDecimal platformFee;

    /** 陪玩师实得金额 */
    @TableField("companion_income")
    private BigDecimal companionIncome;

    /** 扣款项（违规罚款等） */
    @TableField("deduction_amount")
    private BigDecimal deductionAmount;

    /** 扣款原因 */
    @TableField("deduction_reason")
    private String deductionReason;

    /** 结算状态：PENDING-待结算, PROCESSING-结算中, COMPLETED-已结算 */
    @TableField("settlement_status")
    private String settlementStatus;

    /** 实际结算时间 */
    @TableField("settled_at")
    private java.time.LocalDateTime settledAt;

    /** 收款方式 */
    @TableField("payment_method")
    private String paymentMethod;

    /** 收款账号 */
    @TableField("payment_account")
    private String paymentAccount;

    /** 陪玩师确认状态：UNCONFIRMED-未确认, CONFIRMED-已确认, DISPUTED-有异议 */
    @TableField("confirm_status")
    private String confirmStatus;

    /** 申诉内容 */
    @TableField("dispute_content")
    private String disputeContent;
}
