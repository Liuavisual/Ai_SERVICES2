package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_order_records")
public class WorkOrderRecord extends BaseEntity {

    private Long workOrderId;
    private String recordType;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String content;
    private String oldStatus;
    private String newStatus;
}
