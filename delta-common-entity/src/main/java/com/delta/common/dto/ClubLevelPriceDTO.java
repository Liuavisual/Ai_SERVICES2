package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 俱乐部等级价格数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "俱乐部等级价格数据传输对象")
public class ClubLevelPriceDTO {

    @Schema(description = "价格ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "陪玩师等级ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long levelId;

    @Schema(description = "价格(元/小时)", example = "88.00")
    private BigDecimal price;
}
