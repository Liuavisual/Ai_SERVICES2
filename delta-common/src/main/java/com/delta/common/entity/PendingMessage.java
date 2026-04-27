package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待处理消息实体（人工客服升级工单）
 * <p>
 * 对应数据库表 pending_messages，当客户触发转人工条件时自动创建，
 * 记录从创建→客服接手→处理完成的完整生命周期。</p>
 *
 * <p><b>三态流转模型：</b></p>
 * <ul>
 *   <li>pending（待处理）→ AI回复"正在安排客服"，等待客服接手</li>
 *   <li>processing（处理中）→ AI完全静默，客服正在服务</li>
 *   <li>resolved（已解决）→ 流程结束，触发满意度评价</li>
 * </ul>
 *
 * @author delta
 */
@Data
@TableName("pending_messages")
public class PendingMessage {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的原始消息ID（messages表） */
    private Long messageId;

    /** 关联的客户ID（users表） */
    private Long userId;

    /** 来源平台：wechat、test等 */
    private String platform;

    /** 触发转人工的关键词 */
    private String keyword;

    /** 介入类型：ORDER/SCHEDULE/SPECIFIC_COMPANION/COMPLAINT/HUMAN_REQUEST/OTHER */
    private String interventionType;

    /** 当前状态：pending-待处理，processing-处理中，resolved-已解决 */
    @TableField("status")
    private String status;

    /** 处理截止时间，超时后自动升级 */
    @TableField("deadline")
    private LocalDateTime deadline;

    /** 升级级别：0-正常，1-超时警告，2-已升级到负责人 */
    @TableField("escalation_level")
    private Integer escalationLevel;

    /** 分配的客服人员ID（sys_user表） */
    @TableField("assigned_cs_user_id")
    private Long assignedCsUserId;

    /** 已发送提醒次数 */
    @TableField("reminder_count")
    private Integer reminderCount;

    /** 实际处理人ID */
    private Long handledBy;

    /** 实际处理时间 */
    private LocalDateTime handledAt;

    /** 处理备注 */
    private String remark;

    /** 对话上下文摘要，转人工时自动生成，帮助客服快速了解客户问题 */
    @TableField("context_summary")
    private String contextSummary;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
