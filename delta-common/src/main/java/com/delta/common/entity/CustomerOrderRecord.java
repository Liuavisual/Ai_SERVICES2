package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户消费记录实体，记录每笔消费详情
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("customer_order_record")
@Table(name = "customer_order_record", indexes = {
    @Index(name = "idx_cor_user_id", columnList = "user_id"),
    @Index(name = "idx_cor_companion_id", columnList = "companion_id"),
    @Index(name = "idx_cor_status", columnList = "status"),
    @Index(name = "idx_cor_order_time", columnList = "order_time")
})
public class CustomerOrderRecord extends BaseEntity {

    private Long userId;

    private Long companionId;

    private String orderType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    private BigDecimal durationHours;

    private BigDecimal amount;

    private String gameType;

    private String companionLevel;

    private String timeSlot;

    private Integer rating;

    private String reviewContent;

    private String status;

    private String remark;

    private Long serviceItemId;

    private Long gameConfigId;

    private Long activityPackageId;

    private String guaranteeResult;
}
