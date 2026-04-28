package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClubLevelPriceVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    @ObfuscatedId
    private Long levelId;
    private String levelName;
    private String levelCode;
    private Integer sortOrder;
    private BigDecimal price;
}
