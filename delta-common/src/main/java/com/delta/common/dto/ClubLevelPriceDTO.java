package com.delta.common.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 俱乐部等级价格数据传输对象
 *
 * @author delta
 */
@Data
public class ClubLevelPriceDTO {

    private Long id;
    /** 陪玩师等级ID */    private Long levelId;
    /** 价格(元/小时) */    private BigDecimal price;
}
