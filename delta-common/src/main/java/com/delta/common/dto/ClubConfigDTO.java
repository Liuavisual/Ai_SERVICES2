package com.delta.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 俱乐部配置数据传输对象
 *
 * @author delta
 */
@Data
public class ClubConfigDTO {

    private Long id;
    /** 俱乐部名称 */    private String clubName;
    /** 俱乐部Logo URL */    private String clubLogo;
    /** 主营游戏 */    private String mainGames;
    /** 服务口号 */    private String serviceSlogan;
    /** 欢迎语 */    private String welcomeMessage;
    /** 联系方式 */    private String contactInfo;
    /** 俱乐部特色 */    private String clubFeatures;
    /** 等级价格列表 */    private List<ClubLevelPriceDTO> levelPrices;

    @Deprecated
    private BigDecimal priceLevelTwo;

    @Deprecated
    private BigDecimal priceLevelOne;

    @Deprecated
    private BigDecimal priceTop;

    @Deprecated
    private BigDecimal priceStar;
}
