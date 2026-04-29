package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 陪玩师等级数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "陪玩师等级数据传输对象")
public class CompanionLevelDTO {

    @Schema(description = "等级ID", example = "1")
    private Long id;

    @Schema(description = "等级名称", example = "金牌陪玩")
    @NotBlank(message = "等级名称不能为空")
    /** 等级名称 */    private String levelName;

    @Schema(description = "等级编码", example = "GOLD", allowableValues = {"BRONZE", "SILVER", "GOLD", "DIAMOND", "STAR"})
    @NotBlank(message = "等级代码不能为空")
    /** 等级编码 */    private String levelCode;

    @Schema(description = "排序序号", example = "3")
    /** 排序序号 */    private Integer sortOrder = 0;

    @Schema(description = "基础价格(元/小时)", example = "88.00")
    /** 基础价格 */    private BigDecimal basePrice;

    @Schema(description = "等级描述", example = "拥有丰富经验和高评分的资深陪玩师")
    /** 等级描述 */    private String description;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    /** 是否启用 */    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getLevelCode() {
        return levelCode;
    }

    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
