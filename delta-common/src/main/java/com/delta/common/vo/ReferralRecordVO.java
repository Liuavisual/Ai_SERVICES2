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
@Schema(description = "裂变推荐记录视图对象")
public class ReferralRecordVO extends BaseVO {

    @Schema(description = "推荐记录ID")
    @ObfuscatedId
    private Long id;

    @Schema(description = "关联活动ID")
    @ObfuscatedId
    private Long campaignId;

    @Schema(description = "活动名称")
    private String campaignName;

    @Schema(description = "推荐人用户ID")
    @ObfuscatedId
    private Long referrerUserId;

    @Schema(description = "推荐人名称")
    private String referrerUserName;

    @Schema(description = "被推荐人用户ID")
    @ObfuscatedId
    private Long refereeUserId;

    @Schema(description = "被推荐人名称")
    private String refereeUserName;

    @Schema(description = "推荐码")
    private String referralCode;

    @Schema(description = "推荐时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime referralTime;

    @Schema(description = "转化状态")
    private String conversionStatus;

    @Schema(description = "转化时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime convertedAt;

    @Schema(description = "奖励类型")
    private String rewardType;

    @Schema(description = "奖励金额")
    private BigDecimal rewardAmount;

    @Schema(description = "奖励发放状态")
    private String rewardStatus;

    @Schema(description = "奖励发放时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rewardIssuedAt;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
