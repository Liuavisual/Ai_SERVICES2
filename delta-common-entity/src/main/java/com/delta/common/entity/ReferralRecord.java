package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 裂变推荐记录实体
 * <p>
 * 对应数据库表 referral_record，记录老客户推荐新客户的裂变数据。
 * 支持报告第六节推广策略中的"老带新"裂变拉新奖励机制。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("referral_record")
@Table(name = "referral_record", indexes = {
        @Index(name = "idx_rr_referrer_id", columnList = "referrer_user_id"),
        @Index(name = "idx_rr_referee_id", columnList = "referee_user_id"),
        @Index(name = "idx_rr_status", columnList = "status"),
        @Index(name = "idx_rr_campaign_id", columnList = "campaign_id")
})
public class ReferralRecord extends BaseEntity {

    /** 关联营销活动ID */
    @TableField("campaign_id")
    private Long campaignId;

    /** 推荐人用户ID（老客户） */
    @TableField("referrer_user_id")
    private Long referrerUserId;

    /** 被推荐人用户ID（新客户） */
    @TableField("referee_user_id")
    private Long refereeUserId;

    /** 推荐码 */
    @TableField("referral_code")
    private String referralCode;

    /** 推荐时间 */
    @TableField("referral_time")
    private LocalDateTime referralTime;

    /** 转化状态：PENDING-待注册, REGISTERED-已注册, TRIALING-试用中, SUBSCRIBED-已付费 */
    @TableField("conversion_status")
    private String conversionStatus;

    /** 转化时间（注册/付费时间） */
    @TableField("converted_at")
    private LocalDateTime convertedAt;

    /** 推荐人奖励类型：MONTH_FREE-赠送月会员, CASH-现金奖励, POINTS-积分奖励 */
    @TableField("reward_type")
    private String rewardType;

    /** 推荐人奖励金额（元） */
    @TableField("reward_amount")
    private java.math.BigDecimal rewardAmount;

    /** 奖励发放状态：PENDING-待发放, ISSUED-已发放, CANCELLED-已取消 */
    @TableField("reward_status")
    private String rewardStatus;

    /** 奖励发放时间 */
    @TableField("reward_issued_at")
    private LocalDateTime rewardIssuedAt;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
