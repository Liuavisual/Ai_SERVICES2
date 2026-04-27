package com.delta.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceItemDTO {

    private Long id;

    @NotNull(message = "俱乐部ID不能为空")
    private Long clubConfigId;

    private Long gameConfigId;

    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    @NotBlank(message = "项目编码不能为空")
    private String itemCode;

    @NotBlank(message = "分类不能为空")
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
