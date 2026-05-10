package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户画像实体，基于RFM模型和消费行为分析
 * <p>
 * 数据来源：仅店内消费记录 + 客服/陪玩交互数据，不涉及客户隐私信息。
 * 画像维度：RFM价值评估、消费行为、服务偏好、交互行为、满意度、生命周期、需求分类。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_profile")
@Table(name = "customer_profile", indexes = {
        @Index(name = "idx_customer_profile_user_id", columnList = "user_id")
})
public class CustomerProfile extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** RFM最近消费得分 */
    private Integer rfmRecencyScore;

    /** RFM消费频率得分 */
    private Integer rfmFrequencyScore;

    /** RFM消费金额得分 */
    private Integer rfmMonetaryScore;

    /** RFM综合得分 */
    private Integer rfmTotalScore;

    /** RFM分群标签 */
    private String rfmSegment;

    /** 总订单数 */
    private Integer totalOrders;

    /** 总消费金额 */
    private BigDecimal totalSpent;

    /** 平均订单金额 */
    private BigDecimal avgOrderAmount;

    /** 最大订单金额 */
    private BigDecimal maxOrderAmount;

    /** 消费趋势 */
    private String spendingTrend;

    /** 复购率 */
    private BigDecimal repurchaseRate;

    /** 预估客户终身价值 */
    private BigDecimal estimatedLtv;

    /** 平均服务时长 */
    private BigDecimal avgServiceDuration;

    /** 最后下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOrderAt;

    /** 最喜欢的陪玩师ID */
    private Long favoriteCompanionId;

    /** 最喜欢的游戏类型 */
    private String favoriteGameType;

    /** 偏好时段 */
    private String preferredTimeSlot;

    /** 偏好陪玩师等级 */
    private String preferredCompanionLevel;

    /** 偏好订单类型 */
    private String preferredOrderType;

    /** 陪玩师多样性 */
    private Integer companionDiversity;

    /** 首次接触时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstContactAt;

    /** 最后活跃时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;

    /** 活跃天数 */
    private Integer activeDays;

    /** 总消息数 */
    private Integer totalMessages;

    /** AI交互次数 */
    private Integer aiInteractionCount;

    /** 人工交互次数 */
    private Integer manualInteractionCount;

    /** AI交互占比 */
    private BigDecimal aiRatio;

    /** 转人工次数 */
    private Integer humanHandoffCount;

    /** 最常见转人工原因 */
    private String topHandoffReason;

    /** 情绪触发次数 */
    private Integer emotionTriggerCount;

    /** 下单意向次数 */
    private Integer orderIntentCount;

    /** 满意度评分 */
    private BigDecimal satisfactionScore;

    /** 满意度趋势 */
    private String satisfactionTrend;

    /** 投诉次数 */
    private Integer complaintCount;

    /** 退款次数 */
    private Integer refundCount;

    /** 平均评分 */
    private BigDecimal avgRating;

    /** 生命周期阶段 */
    private String lifecycleStage;

    /** 会员等级 */
    private String memberLevel;

    /** 风险等级 */
    private String riskLevel;

    /** 流失风险评分 */
    private BigDecimal churnRiskScore;

    /** 主要需求类型 */
    private String primaryNeedType;

    /** 需求标签 */
    private String needTags;

    /** 标签 */
    private String tags;

    /** 备注 */
    private String remark;

    /** 游戏偏好列表（JSON数组格式，如 ["delta_force","league_of_legends"]） */
    private String gamePreferences;

    /** 消费能力等级：HIGH-高(月均>5000), MEDIUM-中(月均1000-5000), LOW-低(月均<1000) */
    private String spendingLevel;

    /** 专属客服ID */
    private Long assignedCsUserId;
}
