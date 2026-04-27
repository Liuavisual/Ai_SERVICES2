package com.delta.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceItemVO {

    private Long id;
    private Long clubConfigId;
    private Long gameConfigId;
    private String gameName;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<ServicePriceRuleVO> priceRules;
}
