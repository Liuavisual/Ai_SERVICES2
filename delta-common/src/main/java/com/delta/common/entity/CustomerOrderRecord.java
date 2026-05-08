package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_order_record")
public class CustomerOrderRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long customerId;

    private Long orderId;

    private String recordType;

    @TableField(value = "content", jdbcType = JdbcType.LONGVARCHAR)
    private String content;

    private Long operatorId;

    private String operatorName;

    private Long userId;

    private Long companionId;

    private String orderType;

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
