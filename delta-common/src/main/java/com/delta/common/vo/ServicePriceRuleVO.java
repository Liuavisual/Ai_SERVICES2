package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServicePriceRuleVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long serviceItemId;
    @ObfuscatedId
    private Long companionLevelId;
    private String levelName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String priceUnit;
    private Integer enabled;
}
