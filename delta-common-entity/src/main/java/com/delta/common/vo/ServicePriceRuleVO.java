package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "服务价格规则视图对象")
public class ServicePriceRuleVO extends BaseVO {

    @Schema(description = "价格规则ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "服务项目ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long serviceItemId;

    @Schema(description = "陪玩师等级ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long companionLevelId;

    @Schema(description = "等级名称", example = "金牌陪玩")
    private String levelName;

    @Schema(description = "价格", example = "88.00")
    private BigDecimal price;

    @Schema(description = "原价", example = "128.00")
    private BigDecimal originalPrice;

    @Schema(description = "价格单位", example = "元/小时", allowableValues = {"元/小时", "元/次", "元/局"})
    private String priceUnit;

    @Schema(description = "启用状态", example = "1", allowableValues = {"0", "1"})
    private Integer enabled;
}
