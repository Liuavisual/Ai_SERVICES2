package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("game_config")
public class GameConfig extends BaseEntity {

    /** 俱乐部配置ID */
    private Long clubConfigId;

    /** 游戏名称 */
    private String gameName;

    /** 游戏编码 */
    private String gameCode;

    /** 图标URL */
    @TableField("game_icon")
    private String iconUrl;

    /** 描述 */
    @TableField("game_desc")
    private String description;

    /** 自定义设置 */
    private String customSettings;

    /** 游戏类型 */
    private String gameType;

    /** 基础时价 */
    @TableField("base_hourly_price")
    private BigDecimal baseHourlyPrice;

    /** 是否启用 */
    private Integer enabled;

    /** 排序序号 */
    private Integer sortOrder;
}
