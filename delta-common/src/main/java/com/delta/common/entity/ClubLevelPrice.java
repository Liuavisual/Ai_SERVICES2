package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 俱乐部等级价格关联实体
 * <p>
 * 对应数据库表 club_level_prices，建立俱乐部配置与陪玩师等级之间的价格关联，
 * 支持不同俱乐部对同一等级设置不同价格。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("club_level_prices")
public class ClubLevelPrice extends BaseEntity {

    /** 关联的俱乐部配置ID */
    private Long clubConfigId;

    /** 关联的陪玩师等级ID */
    private Long levelId;

    /** 该等级在该俱乐部下的价格（元/小时） */
    private BigDecimal price;
}
