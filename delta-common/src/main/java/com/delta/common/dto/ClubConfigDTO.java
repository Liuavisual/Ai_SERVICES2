package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 俱乐部配置数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "俱乐部配置数据传输对象")
public class ClubConfigDTO {

    @Schema(description = "俱乐部ID", example = "1")
    private Long id;

    @Schema(description = "俱乐部名称", example = "星辰俱乐部")
    /** 俱乐部名称 */    private String clubName;

    @Schema(description = "俱乐部Logo URL", example = "https://example.com/logo/star.png")
    /** 俱乐部Logo URL */    private String clubLogo;

    @Schema(description = "主营游戏", example = "王者荣耀,和平精英,原神")
    /** 主营游戏 */    private String mainGames;

    @Schema(description = "服务口号", example = "专业陪玩，快乐无限")
    /** 服务口号 */    private String serviceSlogan;

    @Schema(description = "欢迎语", example = "欢迎来到星辰俱乐部！")
    /** 欢迎语 */    private String welcomeMessage;

    @Schema(description = "联系方式", example = "微信:star_club")
    /** 联系方式 */    private String contactInfo;

    @Schema(description = "俱乐部特色", example = "24小时在线,专业陪玩师团队")
    /** 俱乐部特色 */    private String clubFeatures;

    @Schema(description = "等级价格列表")
    /** 等级价格列表 */    private List<ClubLevelPriceDTO> levelPrices;

    @Deprecated
    @Schema(description = "二级价格(已废弃)", example = "68.00", deprecated = true)
    private BigDecimal priceLevelTwo;

    @Deprecated
    @Schema(description = "一级价格(已废弃)", example = "88.00", deprecated = true)
    private BigDecimal priceLevelOne;

    @Deprecated
    @Schema(description = "顶级价格(已废弃)", example = "128.00", deprecated = true)
    private BigDecimal priceTop;

    @Deprecated
    @Schema(description = "星级价格(已废弃)", example = "168.00", deprecated = true)
    private BigDecimal priceStar;
}
