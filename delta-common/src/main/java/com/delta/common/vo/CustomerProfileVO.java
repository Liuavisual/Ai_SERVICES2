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
@Schema(description = "客户画像视图对象")
public class CustomerProfileVO extends BaseVO {

    @Schema(description = "画像ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "用户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar/001.jpg")
    private String avatar;

    @Schema(description = "平台标识", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    private String platform;

    @Schema(description = "RFM最近消费评分", example = "5")
    private Integer rfmRecencyScore;

    @Schema(description = "RFM消费频率评分", example = "4")
    private Integer rfmFrequencyScore;

    @Schema(description = "RFM消费金额评分", example = "3")
    private Integer rfmMonetaryScore;

    @Schema(description = "RFM综合评分", example = "12")
    private Integer rfmTotalScore;

    @Schema(description = "RFM客户分群", example = "高价值客户")
    private String rfmSegment;

    @Schema(description = "总订单数", example = "25")
    private Integer totalOrders;

    @Schema(description = "总消费金额", example = "5680.00")
    private BigDecimal totalSpent;

    @Schema(description = "平均订单金额", example = "227.20")
    private BigDecimal avgOrderAmount;

    @Schema(description = "最大订单金额", example = "500.00")
    private BigDecimal maxOrderAmount;

    @Schema(description = "消费趋势", example = "UP", allowableValues = {"UP", "STABLE", "DOWN"})
    private String spendingTrend;

    @Schema(description = "复购率", example = "0.72")
    private BigDecimal repurchaseRate;

    @Schema(description = "预估生命周期价值", example = "12000.00")
    private BigDecimal estimatedLtv;

    @Schema(description = "平均服务时长(小时)", example = "2.5")
    private BigDecimal avgServiceDuration;

    @Schema(description = "最近下单时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOrderAt;

    @Schema(description = "最喜爱的陪玩师ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long favoriteCompanionId;

    @Schema(description = "最喜爱的陪玩师名称", example = "小明同学")
    private String favoriteCompanionName;

    @Schema(description = "最喜爱的游戏类型", example = "王者荣耀")
    private String favoriteGameType;

    @Schema(description = "偏好时间段", example = "10:00-12:00")
    private String preferredTimeSlot;

    @Schema(description = "偏好陪玩师等级", example = "GOLD", allowableValues = {"BRONZE", "SILVER", "GOLD", "DIAMOND", "STAR"})
    private String preferredCompanionLevel;

    @Schema(description = "偏好订单类型", example = "陪玩", allowableValues = {"陪玩", "语音", "视频"})
    private String preferredOrderType;

    @Schema(description = "陪玩师多样性(不同陪玩师数量)", example = "5")
    private Integer companionDiversity;

    @Schema(description = "首次联系时间", example = "2025-06-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstContactAt;

    @Schema(description = "最后活跃时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;

    @Schema(description = "活跃天数", example = "120")
    private Integer activeDays;

    @Schema(description = "总消息数", example = "560")
    private Integer totalMessages;

    @Schema(description = "AI交互次数", example = "200")
    private Integer aiInteractionCount;

    @Schema(description = "人工交互次数", example = "80")
    private Integer manualInteractionCount;

    @Schema(description = "AI交互占比", example = "0.71")
    private BigDecimal aiRatio;

    @Schema(description = "转人工次数", example = "15")
    private Integer humanHandoffCount;

    @Schema(description = "主要转人工原因", example = "退款问题")
    private String topHandoffReason;

    @Schema(description = "情绪触发次数", example = "8")
    private Integer emotionTriggerCount;

    @Schema(description = "下单意向次数", example = "30")
    private Integer orderIntentCount;

    @Schema(description = "满意度评分", example = "4.5")
    private BigDecimal satisfactionScore;

    @Schema(description = "满意度趋势", example = "UP", allowableValues = {"UP", "STABLE", "DOWN"})
    private String satisfactionTrend;

    @Schema(description = "投诉次数", example = "2")
    private Integer complaintCount;

    @Schema(description = "退款次数", example = "1")
    private Integer refundCount;

    @Schema(description = "平均评分", example = "4.8")
    private BigDecimal avgRating;

    @Schema(description = "生命周期阶段", example = "ACTIVE", allowableValues = {"NEW", "ACTIVE", "AT_RISK", "CHURNED", "REACTIVATED"})
    private String lifecycleStage;

    @Schema(description = "会员等级", example = "GOLD", allowableValues = {"NORMAL", "SILVER", "GOLD", "DIAMOND"})
    private String memberLevel;

    @Schema(description = "风险等级", example = "LOW", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private String riskLevel;

    @Schema(description = "流失风险评分", example = "0.15")
    private BigDecimal churnRiskScore;

    @Schema(description = "主要需求类型", example = "SOCIAL", allowableValues = {"SOCIAL", "GAMING", "EMOTIONAL", "ENTERTAINMENT"})
    private String primaryNeedType;

    @Schema(description = "需求标签", example = "社交,竞技,休闲")
    private String needTags;

    @Schema(description = "标签", example = "高价值用户,VIP")
    private String tags;

    @Schema(description = "备注", example = "重点客户，需定期回访")
    private String remark;

    @Schema(description = "分配的客服名称", example = "客服小李")
    private String assignedCsUserName;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
