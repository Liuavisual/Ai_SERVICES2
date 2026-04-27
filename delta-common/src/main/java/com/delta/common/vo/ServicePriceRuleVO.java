package com.delta.common.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicePriceRuleVO {

    private Long id;
    private Long serviceItemId;
    private Long companionLevelId;
    private String levelName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String priceUnit;
    private Integer enabled;
}
