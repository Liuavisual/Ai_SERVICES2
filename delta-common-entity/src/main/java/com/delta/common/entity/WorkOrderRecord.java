package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_order_records")
@Table(name = "work_order_records", indexes = {
    @Index(name = "idx_wor_work_order_id", columnList = "work_order_id"),
    @Index(name = "idx_wor_operator_id", columnList = "operator_id")
})
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
