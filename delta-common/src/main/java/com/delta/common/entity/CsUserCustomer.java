package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cs_user_customer")
public class CsUserCustomer extends BaseEntity {

    private Long csUserId;

    private Long customerUserId;

    private String assignType;

    private LocalDateTime assignedAt;

    private Long assignedBy;

    private String status;
}
