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
@Schema(description = "陪玩师等级视图对象")
public class CompanionLevelVO extends BaseVO {

    @Schema(description = "等级ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "等级名称", example = "金牌陪玩")
    private String levelName;

    @Schema(description = "等级编码", example = "GOLD", allowableValues = {"BRONZE", "SILVER", "GOLD", "DIAMOND", "STAR"})
    private String levelCode;

    @Schema(description = "排序序号", example = "3")
    private Integer sortOrder;

    @Schema(description = "基础价格(元/小时)", example = "88.00")
    private BigDecimal basePrice;

    @Schema(description = "等级描述", example = "拥有丰富经验和高评分的资深陪玩师")
    private String description;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
