package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 俱乐部订阅实体
 * <p>
 * 对应数据库表 club_subscription，记录每个俱乐部的当前订阅状态。
 * 支持月度/年度订阅、试用期管理、自动续费标记。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("club_subscription")
@Table(name = "club_subscription", indexes = {
        @Index(name = "idx_cs_club_config_id", columnList = "club_config_id"),
        @Index(name = "idx_cs_status", columnList = "status"),
        @Index(name = "idx_cs_expire_at", columnList = "expire_at")
})
public class ClubSubscription extends BaseEntity {

    /** 俱乐部配置ID */
    @TableField("club_config_id")
    private Long clubConfigId;

    /** 定价方案ID */
    @TableField("plan_id")
    private Long planId;

    /** 订阅状态：TRIAL-试用中, ACTIVE-生效中, EXPIRED-已过期, CANCELLED-已取消 */
    @TableField("status")
    private String status;

    /** 订阅开始时间 */
    @TableField("start_at")
    private LocalDateTime startAt;

    /** 订阅到期时间 */
    @TableField("expire_at")
    private LocalDateTime expireAt;

    /** 试用到期时间 */
    @TableField("trial_end_at")
    private LocalDateTime trialEndAt;

    /** 是否自动续费 */
    @TableField("auto_renew")
    private Boolean autoRenew;

    /** 实付金额 */
    @TableField("paid_amount")
    private BigDecimal paidAmount;

    /** 支付方式 */
    @TableField("payment_method")
    private String paymentMethod;

    /** 支付流水号 */
    @TableField("payment_transaction_id")
    private String paymentTransactionId;

    /** 支付时间 */
    @TableField("paid_at")
    private LocalDateTime paidAt;
}
