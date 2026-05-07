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
@Schema(description = "定价方案视图对象")
public class PricingPlanVO extends BaseVO {

    @Schema(description = "方案ID")
    @ObfuscatedId
    private Long id;

    @Schema(description = "方案编码：BASIC/PRO/ENTERPRISE")
    private String planCode;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "月费价格")
    private BigDecimal monthlyPrice;

    @Schema(description = "年费价格")
    private BigDecimal yearlyPrice;

    @Schema(description = "按次付费单价")
    private BigDecimal perCallPrice;

    @Schema(description = "日会员价格")
    private BigDecimal dailyPrice;

    @Schema(description = "陪玩师数量上限")
    private Integer maxCompanions;

    @Schema(description = "月消息量上限")
    private Integer maxMonthlyMessages;

    @Schema(description = "AI人格模板数量上限")
    private Integer maxPersonalityTemplates;

    @Schema(description = "情绪智能等级")
    private String emotionIntelligenceLevel;

    @Schema(description = "是否包含智能派单")
    private Boolean includeSmartDispatch;

    @Schema(description = "是否包含全流程质检")
    private Boolean includeFullQualityCheck;

    @Schema(description = "是否包含数据分析")
    private Boolean includeAnalytics;

    @Schema(description = "是否支持自定义品牌")
    private Boolean includeBrandCustom;

    @Schema(description = "是否支持API接入")
    private Boolean includeApiAccess;

    @Schema(description = "功能描述")
    private String features;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态：1-启用 0-禁用")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
