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

    @TableField(exist = false)
    private Long userId;

    @TableField(exist = false)
    private Long companionId;

    @TableField(exist = false)
    private String orderType;

    @TableField(exist = false)
    private LocalDateTime orderTime;

    @TableField(exist = false)
    private BigDecimal durationHours;

    @TableField(exist = false)
    private BigDecimal amount;

    @TableField(exist = false)
    private String gameType;

    @TableField(exist = false)
    private String companionLevel;

    @TableField(exist = false)
    private String timeSlot;

    @TableField(exist = false)
    private Integer rating;

    @TableField(exist = false)
    private String reviewContent;

    @TableField(exist = false)
    private String status;

    @TableField(exist = false)
    private String remark;

    @TableField(exist = false)
    private Long serviceItemId;

    @TableField(exist = false)
    private Long gameConfigId;

    @TableField(exist = false)
    private Long activityPackageId;

    @TableField(exist = false)
    private String guaranteeResult;
}
