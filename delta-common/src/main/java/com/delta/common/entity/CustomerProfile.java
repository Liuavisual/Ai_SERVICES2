package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("customer_profile")
public class CustomerProfile extends BaseEntity {

    private Long userId;

    private Integer rfmRecencyScore;

    private Integer rfmFrequencyScore;

    private Integer rfmMonetaryScore;

    private Integer rfmTotalScore;

    private String rfmSegment;

    private Integer totalOrders;

    private BigDecimal totalSpent;

    private BigDecimal avgOrderAmount;

    private BigDecimal maxOrderAmount;

    private String spendingTrend;

    private BigDecimal repurchaseRate;

    private BigDecimal estimatedLtv;

    private BigDecimal avgServiceDuration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOrderAt;

    private Long favoriteCompanionId;

    private String favoriteGameType;

    private String preferredTimeSlot;

    private String preferredCompanionLevel;

    private String preferredOrderType;

    private Integer companionDiversity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstContactAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;

    private Integer activeDays;

    private Integer totalMessages;

    private Integer aiInteractionCount;

    private Integer manualInteractionCount;

    private BigDecimal aiRatio;

    private Integer humanHandoffCount;

    private String topHandoffReason;

    private Integer emotionTriggerCount;

    private Integer orderIntentCount;

    private BigDecimal satisfactionScore;

    private String satisfactionTrend;

    private Integer complaintCount;

    private Integer refundCount;

    private BigDecimal avgRating;

    private String lifecycleStage;

    private String memberLevel;

    private String riskLevel;

    private BigDecimal churnRiskScore;

    private String primaryNeedType;

    private String needTags;

    private String tags;

    private String remark;
}
