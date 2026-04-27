package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_item")
public class ServiceItem extends BaseEntity {

    private Long clubConfigId;
    private Long gameConfigId;
    private String itemName;
    private String itemCode;
    private String category;
    private String description;
    private BigDecimal basePrice;
    private String priceUnit;
    private BigDecimal minDuration;
    private String guaranteeText;
    private String refundPolicy;
    private Integer sortOrder;
    private Integer enabled;
}
