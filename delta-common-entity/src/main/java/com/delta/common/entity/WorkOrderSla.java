package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 工单SLA追踪实体
 * <p>
 * 对应数据库表 work_order_sla，追踪工单的SLA（服务等级协议）状态。
 * 当工单超时未处理时，触发告警通知（P1-5改进）。
 * </p>
 *
 * @author 刘建国
 */
@Data
@TableName("work_order_sla")
@Table(name = "work_order_sla", indexes = {
        @Index(name = "idx_wsla_work_order_id", columnList = "work_order_id"),
        @Index(name = "idx_wsla_status", columnList = "status"),
        @Index(name = "idx_wsla_deadline", columnList = "deadline_time")
})
public class WorkOrderSla {

    /** 主键 */
    @TableField("id")
    private Long id;

    /** 关联工单ID */
    @TableField("work_order_id")
    private Long workOrderId;

    /** 优先级：URGENT(15分钟) / HIGH(30分钟) / NORMAL(60分钟) / LOW(120分钟) */
    @TableField("priority_level")
    private String priorityLevel;

    /** SLA截止时间 */
    @TableField("deadline_time")
    private java.time.LocalDateTime deadlineTime;

    /** 预警时间（截止前N分钟预警） */
    @TableField("warn_time")
    private java.time.LocalDateTime warnTime;

    /** 状态：PENDING-等待中 / WARNED-已预警 / BREACHED-已超时 / COMPLETED-已完成 */
    @TableField("status")
    private String status;

    /** 是否已发送告警通知 */
    @TableField("alert_sent")
    private Integer alertSent;

    /** 超时时长（分钟，实际处理时间 - 截止时间） */
    @TableField("breach_minutes")
    private Integer breachMinutes;
}
