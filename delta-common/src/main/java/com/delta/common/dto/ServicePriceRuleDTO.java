package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务价格规则数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "服务价格规则数据传输对象")
public class ServicePriceRuleDTO {

    @Schema(description = "价格规则ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "服务项目ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "服务项目ID不能为空")
    @ObfuscatedId
    private Long serviceItemId;

    @Schema(description = "陪玩师等级ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long companionLevelId;

    @Schema(description = "价格", example = "88.00")
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @Schema(description = "原价", example = "128.00")
    private BigDecimal originalPrice;

    @Schema(description = "价格单位", example = "元/小时", allowableValues = {"元/小时", "元/次", "元/局"})
    private String priceUnit;

    @Schema(description = "启用状态", example = "1", allowableValues = {"0", "1"})
    private Integer enabled;
}
