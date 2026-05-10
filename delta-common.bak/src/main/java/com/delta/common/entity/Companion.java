package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;

/**
 * 陪玩师实体
 * <p>
 * 对应数据库表 companions，记录陪玩师的基本信息，
 * 包括等级、游戏类型、价格、服务标签、支持游戏等。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("companions")
@Table(name = "companions", indexes = {
        @Index(name = "idx_companions_level_id", columnList = "level_id"),
        @Index(name = "idx_companions_enabled", columnList = "enabled"),
        @Index(name = "idx_companions_game_type", columnList = "game_type"),
        @Index(name = "idx_companions_enabled_game_type", columnList = "enabled,game_type")
})
public class Companion extends BaseEntity {

    /** 真实姓名 */
    private String realName;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 微信号 */
    private String wechat;

    /** 等级ID */
    private Long levelId;

    /** 头像URL */
    private String avatar;

    /** 游戏类型 */
    private String gameType;

    /** 个人简介 */
    @TableField(value = "description", jdbcType = JdbcType.LONGVARCHAR)
    private String description;

    /** 价格（元/小时） */
    private BigDecimal price;

    /** 是否启用：1-启用，0-禁用 */
    private Integer enabled;

    /** 服务标签 */
    @TableField(value = "service_tags", jdbcType = JdbcType.LONGVARCHAR)
    private String serviceTags;

    /** 支持游戏列表 */
    @TableField(value = "supported_games", jdbcType = JdbcType.LONGVARCHAR)
    private String supportedGames;

    /** K/D比率 */
    private BigDecimal kdRatio;

    /** 段位等级 */
    private String rankLevel;

    /** 语音样本URL */
    private String voiceSampleUrl;

    /** 平均评分 */
    private BigDecimal ratingAvg;

    /** 订单数量 */
    private Integer orderCount;
}
