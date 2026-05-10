package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_price_rule")
@Table(name = "service_price_rule", indexes = {
    @Index(name = "idx_spr_service_item_id", columnList = "service_item_id"),
    @Index(name = "idx_spr_companion_level_id", columnList = "companion_level_id")
})
public class ServicePriceRule extends BaseEntity {

    private Long serviceItemId;
    private Long companionLevelId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String priceUnit;
    private Integer enabled;
}
