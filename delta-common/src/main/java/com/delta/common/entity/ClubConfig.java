package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 俱乐部配置实体
 * <p>
 * 对应数据库表 club_config，存储陪玩俱乐部的基本信息和定价策略，
 * 包括俱乐部名称、Logo、主营游戏、服务口号、各等级价格等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("club_config")
public class ClubConfig extends BaseEntity {

    /** 俱乐部名称 */
    private String clubName;

    /** 俱乐部Logo URL */
    private String clubLogo;

    /** 主营游戏，多个游戏用逗号分隔 */
    private String mainGames;

    /** 服务口号/标语 */
    private String serviceSlogan;

    /** 欢迎语，新客户关注时自动发送 */
    private String welcomeMessage;

    /** 联系方式 */
    private String contactInfo;

    /** 二品陪玩师价格（元/小时） */
    private BigDecimal priceLevelTwo;

    /** 一品陪玩师价格（元/小时） */
    private BigDecimal priceLevelOne;

    /** 顶尖陪玩师价格（元/小时） */
    private BigDecimal priceTop;

    /** 明星陪玩师价格（元/小时） */
    private BigDecimal priceStar;

    private String clubFeatures;

    private String customLevelNames;

    private String servicePromise;

    private String refundPolicy;

    private BigDecimal memberDiscount;

    private String rechargeBonus;

    private String customWelcomeTemplate;

    private String aiPersonality;
}
