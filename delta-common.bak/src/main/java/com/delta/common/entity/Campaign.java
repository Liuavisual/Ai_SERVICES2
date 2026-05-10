package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 营销活动实体
 * <p>
 * 对应数据库表 campaign，管理系统中的各类营销活动。
 * 支持报告第六节推广策略中的试用推广、裂变拉新、节日营销等活动管理。
 * </p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("campaign")
@Table(name = "campaign", indexes = {
        @Index(name = "idx_cam_club_config_id", columnList = "club_config_id"),
        @Index(name = "idx_cam_status", columnList = "status"),
        @Index(name = "idx_cam_start_at", columnList = "start_at")
})
public class Campaign extends BaseEntity {

    /** 俱乐部配置ID */
    @TableField("club_config_id")
    private Long clubConfigId;

    /** 活动名称 */
    @TableField("campaign_name")
    private String campaignName;

    /** 活动类型：TRIAL-试用推广, REFERRAL-裂变拉新, HOLIDAY-节日营销, RECALL-复购唤醒, OTHER-其他 */
    @TableField("campaign_type")
    private String campaignType;

    /** 活动描述 */
    @TableField("description")
    private String description;

    /** 活动开始时间 */
    @TableField("start_at")
    private LocalDateTime startAt;

    /** 活动结束时间 */
    @TableField("end_at")
    private LocalDateTime endAt;

    /** 目标拉新人数 */
    @TableField("target_new_users")
    private Integer targetNewUsers;

    /** 实际拉新人数 */
    @TableField("actual_new_users")
    private Integer actualNewUsers;

    /** 活动预算（元） */
    @TableField("budget")
    private java.math.BigDecimal budget;

    /** 实际花费（元） */
    @TableField("actual_cost")
    private java.math.BigDecimal actualCost;

    /** 奖励方案描述 */
    @TableField("reward_rules")
    private String rewardRules;

    /** 活动状态：DRAFT-草稿, ACTIVE-进行中, PAUSED-已暂停, ENDED-已结束, CANCELLED-已取消 */
    @TableField("status")
    private String status;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
