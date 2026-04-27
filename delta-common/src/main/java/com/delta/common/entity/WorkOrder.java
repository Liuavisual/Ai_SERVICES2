package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_orders")
public class WorkOrder extends BaseEntity {

    private String orderNo;
    private Long sourceId;
    private String orderType;
    private String priority;
    private String platform;
    private Long userId;
    private String customerName;
    private String customerContact;
    private String customerLevel;
    private String serviceType;
    private String serviceStatus;
    private String problemDetail;
    private String problemCategory;
    private String triggerKeyword;
    private String contextSummary;
    private Long assignedCsUserId;
    private String assignedCsName;
    private Long handlerId;
    private String handlerName;
    private String handleResult;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    private Integer escalationLevel;
    private Integer reminderCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedAt;
    private Long relatedOrderId;
    private Long relatedCompanionId;
    private Integer satisfactionScore;
    private String satisfactionRemark;
}
