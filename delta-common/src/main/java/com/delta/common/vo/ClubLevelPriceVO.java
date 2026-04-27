package com.delta.common.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClubLevelPriceVO {

    private Long id;
    private Long levelId;
    private String levelName;
    private String levelCode;
    private Integer sortOrder;
    private BigDecimal price;
}
