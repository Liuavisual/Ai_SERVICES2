package com.delta.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单状态变更历史实体
 *
 * @author 刘建国
 */
@Data
public class OrderStatusHistory {

    /** 主键ID */
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 变更前状态 */
    private String fromStatus;

    /** 变更后状态 */
    private String toStatus;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 操作人角色 */
    private String operatorRole;

    /** 变更原因 */
    private String reason;

    /** 创建时间 */
    private LocalDateTime createdAt;
}