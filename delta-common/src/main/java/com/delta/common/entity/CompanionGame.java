package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * 陪玩师-游戏关联实体
 * <p>
 * 对应数据库表 companion_game，实现陪玩师与游戏的多对多关联。
 * 解决一个陪玩师可擅长多款游戏的业务需求（P1-3改进）。
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("companion_game")
@Table(name = "companion_game", indexes = {
        @Index(name = "idx_cg_companion_id", columnList = "companion_id"),
        @Index(name = "idx_cg_game_code", columnList = "game_code")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"companion_id", "game_code"})
})
public class CompanionGame {

    /** 主键 */
    @TableField("id")
    private Long id;

    /** 陪玩师ID */
    @TableField("companion_id")
    private Long companionId;

    /** 游戏编码（如delta_force、league_of_legends） */
    @TableField("game_code")
    private String gameCode;

    /** 该游戏的熟练度等级(1-5) */
    @TableField("proficiency")
    private Integer proficiency;

    /** 该游戏的段位排名 */
    @TableField("rank_level")
    private String rankLevel;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
