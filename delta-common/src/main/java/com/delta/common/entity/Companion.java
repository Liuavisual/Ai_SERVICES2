package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 陪玩师实体
 * <p>
 * 对应数据库表 companions，存储陪玩师的个人信息和业务属性，
 * 包括真实姓名、昵称、联系方式、等级、擅长游戏、定价等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("companions")
public class Companion extends BaseEntity {

    /** 真实姓名 */
    private String realName;

    /** 陪玩师昵称（对外展示名） */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 微信号 */
    private String wechat;

    /** 关联的陪玩师等级ID */
    private Long levelId;

    /** 头像URL */
    private String avatar;

    /** 擅长游戏类型，如"三角洲行动" */
    private String gameType;

    /** 个人简介/自我介绍 */
    private String description;

    /** 每小时价格（元） */
    private BigDecimal price;

    /** 是否启用：1-启用，0-禁用 */
    private Integer enabled;

    @TableLogic
    private Integer deleted;

    private String serviceTags;

    private String supportedGames;

    private BigDecimal kdRatio;

    private String rankLevel;

    private String voiceSampleUrl;

    private BigDecimal ratingAvg;

    private Integer orderCount;
}
