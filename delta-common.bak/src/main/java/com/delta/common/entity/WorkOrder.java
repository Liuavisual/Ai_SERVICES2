package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 工单实体
 * <p>
 * 对应数据库表 work_orders，记录客服工单信息，
 * 包括工单类型、优先级、分配状态、处理结果等。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_orders")
@Table(name = "work_orders", indexes = {
        @Index(name = "idx_work_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_work_orders_assigned_cs_user_id", columnList = "assigned_cs_user_id"),
        @Index(name = "idx_work_orders_status", columnList = "status"),
        @Index(name = "idx_work_orders_priority", columnList = "priority"),
        @Index(name = "idx_work_orders_order_type", columnList = "order_type"),
        @Index(name = "idx_work_orders_platform", columnList = "platform"),
        @Index(name = "idx_work_orders_created_at", columnList = "created_at"),
        @Index(name = "idx_work_orders_status_priority", columnList = "status,priority")
})
public class WorkOrder extends BaseEntity {

    /** 工单编号 */
    private String orderNo;

    /** 来源ID */
    private Long sourceId;

    /** 工单类型 */
    private String orderType;

    /** 优先级 */
    private String priority;

    /** 平台 */
    private String platform;

    /** 客户ID */
    private Long userId;

    /** 客户姓名 */
    private String customerName;

    /** 客户联系方式 */
    private String customerContact;

    /** 客户等级 */
    private String customerLevel;

    /** 服务类型 */
    private String serviceType;

    /** 服务状态 */
    private String serviceStatus;

    /** 问题描述 */
    @TableField(value = "problem_detail", jdbcType = JdbcType.LONGVARCHAR)
    private String problemDetail;

    /** 问题分类 */
    private String problemCategory;

    /** 触发关键词 */
    private String triggerKeyword;

    /** 上下文摘要 */
    @TableField(value = "context_summary", jdbcType = JdbcType.LONGVARCHAR)
    private String contextSummary;

    /** 分配的客服ID */
    private Long assignedCsUserId;

    /** 分配的客服名称 */
    private String assignedCsName;

    /** 处理人ID */
    private Long handlerId;

    /** 处理人名称 */
    private String handlerName;

    /** 处理结果 */
    private String handleResult;

    /** 工单状态 */
    private String status;

    /** 截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    /** 升级级别 */
    private Integer escalationLevel;

    /** 提醒次数 */
    private Integer reminderCount;

    /** 解决时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;

    /** 关闭时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedAt;

    /** 关联订单ID */
    private Long relatedOrderId;

    /** 关联陪玩师ID */
    private Long relatedCompanionId;

    /** 满意度评分 */
    private Integer satisfactionScore;

    /** 满意度备注 */
    private String satisfactionRemark;
}
