package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "俱乐部等级价格视图对象")
public class ClubLevelPriceVO extends BaseVO {

    @Schema(description = "价格ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "等级ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long levelId;

    @Schema(description = "等级名称", example = "金牌陪玩")
    private String levelName;

    @Schema(description = "等级编码", example = "GOLD", allowableValues = {"BRONZE", "SILVER", "GOLD", "DIAMOND", "STAR"})
    private String levelCode;

    @Schema(description = "排序序号", example = "3")
    private Integer sortOrder;

    @Schema(description = "价格(元/小时)", example = "88.00")
    private BigDecimal price;
}
