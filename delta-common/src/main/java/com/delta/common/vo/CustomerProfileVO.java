package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerProfileVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long userId;
    private String nickname;
    private String avatar;
    private String platform;
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

    @ObfuscatedId
    private Long favoriteCompanionId;
    private String favoriteCompanionName;
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
    private String assignedCsUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
