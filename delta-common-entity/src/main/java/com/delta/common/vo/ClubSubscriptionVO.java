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
@Schema(description = "俱乐部订阅视图对象")
public class ClubSubscriptionVO extends BaseVO {

    @Schema(description = "订阅ID")
    @ObfuscatedId
    private Long id;

    @Schema(description = "俱乐部配置ID")
    @ObfuscatedId
    private Long clubConfigId;

    @Schema(description = "俱乐部名称")
    private String clubName;

    @Schema(description = "定价方案ID")
    @ObfuscatedId
    private Long planId;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "订阅状态")
    private String status;

    @Schema(description = "订阅开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;

    @Schema(description = "订阅到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireAt;

    @Schema(description = "试用到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime trialEndAt;

    @Schema(description = "是否自动续费")
    private Boolean autoRenew;

    @Schema(description = "实付金额")
    private BigDecimal paidAmount;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
