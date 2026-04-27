package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    private String orderNo;
    private Long userId;
    private Long companionId;
    private String companionName;
    private String serviceType;
    private Long priceRuleId;
    private String orderStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledEnd;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEnd;
    private Integer durationMinutes;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String paymentStatus;
    private String paymentMethod;
    private Long workOrderId;
    private String remark;
}
