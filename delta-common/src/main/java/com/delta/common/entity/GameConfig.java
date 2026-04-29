package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏配置实体
 * <p>
 * 对应数据库表 game_config，存储游戏相关配置信息，
 * 包括游戏名称、编码、类型、排序、图标等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("game_config")
@Table(name = "game_config", indexes = {
        @Index(name = "idx_game_config_club_config_id", columnList = "club_config_id"),
        @Index(name = "idx_game_config_enabled", columnList = "enabled")
})
public class GameConfig extends BaseEntity {

    /** 俱乐部配置ID */
    private Long clubConfigId;

    /** 游戏名称 */
    private String gameName;

    /** 游戏编码 */
    private String gameCode;

    /** 游戏类型 */
    private String gameType;

    /** 是否启用 */
    private Integer enabled;

    /** 排序序号 */
    private Integer sortOrder;

    /** 图标URL */
    private String iconUrl;

    /** 描述 */
    private String description;

    /** 自定义设置 */
    private String customSettings;
}
