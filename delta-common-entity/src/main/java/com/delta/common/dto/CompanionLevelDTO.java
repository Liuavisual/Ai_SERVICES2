package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 陪玩师等级数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "陪玩师等级数据传输对象")
public class CompanionLevelDTO {

    @Schema(description = "等级ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "等级名称", example = "金牌陪玩")
    @NotBlank(message = "等级名称不能为空")
    private String levelName;

    @Schema(description = "等级编码", example = "GOLD", allowableValues = {"BRONZE", "SILVER", "GOLD", "DIAMOND", "STAR"})
    @NotBlank(message = "等级代码不能为空")
    private String levelCode;

    @Schema(description = "排序序号", example = "3")
    private Integer sortOrder = 0;

    @Schema(description = "基础价格(元/小时)", example = "88.00")
    private BigDecimal basePrice;

    @Schema(description = "等级描述", example = "拥有丰富经验和高评分的资深陪玩师")
    private String description;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
