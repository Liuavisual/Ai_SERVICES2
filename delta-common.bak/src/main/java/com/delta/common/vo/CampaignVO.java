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
@Schema(description = "营销活动视图对象")
public class CampaignVO extends BaseVO {

    @Schema(description = "活动ID")
    @ObfuscatedId
    private Long id;

    @Schema(description = "俱乐部配置ID")
    @ObfuscatedId
    private Long clubConfigId;

    @Schema(description = "俱乐部名称")
    private String clubName;

    @Schema(description = "活动名称")
    private String campaignName;

    @Schema(description = "活动类型")
    private String campaignType;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;

    @Schema(description = "活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;

    @Schema(description = "目标拉新人数")
    private Integer targetNewUsers;

    @Schema(description = "实际拉新人数")
    private Integer actualNewUsers;

    @Schema(description = "活动预算")
    private BigDecimal budget;

    @Schema(description = "实际花费")
    private BigDecimal actualCost;

    @Schema(description = "奖励方案描述")
    private String rewardRules;

    @Schema(description = "活动状态")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
