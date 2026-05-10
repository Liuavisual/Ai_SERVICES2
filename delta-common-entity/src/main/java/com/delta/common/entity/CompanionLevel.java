package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 陪玩师等级实体
 * <p>
 * 对应数据库表 companion_levels，定义陪玩师的等级体系，
 * 如二品、一品、顶尖、明星等，每个等级有基础定价。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("companion_levels")
@Table(name = "companion_levels", indexes = {
        @Index(name = "idx_companion_levels_enabled", columnList = "enabled")
})
public class CompanionLevel extends BaseEntity {

    /** 等级名称，如"二品"、"一品"、"顶尖" */
    private String levelName;

    /** 等级编码，如 LEVEL_TWO、LEVEL_ONE、TOP */
    private String levelCode;

    /** 排序序号，数值越小等级越高 */
    private Integer sortOrder;

    /** 该等级基础价格（元/小时） */
    private BigDecimal basePrice;

    /** 等级描述 */
    private String description;

    /** 是否启用：1-启用，0-禁用 */
    private Integer enabled;
}
