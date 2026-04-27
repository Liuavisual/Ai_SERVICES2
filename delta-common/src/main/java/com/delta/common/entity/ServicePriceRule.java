package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_price_rule")
public class ServicePriceRule extends BaseEntity {

    private Long serviceItemId;
    private Long companionLevelId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String priceUnit;
    private Integer enabled;
}
