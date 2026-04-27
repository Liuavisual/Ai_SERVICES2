package com.delta.common.dto;

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
public class CompanionLevelDTO {

    private Long id;

    @NotBlank(message = "等级名称不能为空")
    /** 等级名称 */    private String levelName;

    @NotBlank(message = "等级代码不能为空")
    /** 等级编码 */    private String levelCode;

    /** 排序序号 */    private Integer sortOrder = 0;

    /** 基础价格 */    private BigDecimal basePrice;

    /** 等级描述 */    private String description;

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
