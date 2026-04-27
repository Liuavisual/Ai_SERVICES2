package com.delta.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicePriceRuleDTO {

    private Long id;

    @NotNull(message = "服务项目ID不能为空")
    private Long serviceItemId;

    private Long companionLevelId;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String priceUnit;

    private Integer enabled;
}
