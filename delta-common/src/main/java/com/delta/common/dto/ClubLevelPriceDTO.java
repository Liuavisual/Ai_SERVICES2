package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 俱乐部等级价格数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "俱乐部等级价格数据传输对象")
public class ClubLevelPriceDTO {

    @Schema(description = "价格ID", example = "1")
    private Long id;

    @Schema(description = "陪玩师等级ID", example = "3")
    /** 陪玩师等级ID */    private Long levelId;

    @Schema(description = "价格(元/小时)", example = "88.00")
    /** 价格(元/小时) */    private BigDecimal price;
}
