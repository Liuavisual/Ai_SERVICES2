package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cs_user_customer")
public class CsUserCustomer extends BaseEntity {

    /** 客服用户ID */
    private Long csUserId;

    /** 客户用户ID */
    @TableField("user_id")
    private Long customerUserId;

    /** 平台 */
    private String platform;

    /** 客户名称 */
    @TableField("customer_name")
    private String customerName;

    /** 分配时间 */
    private LocalDateTime assignedAt;

    /** 分配类型 */
    private String assignType;

    /** 分配操作人ID */
    private Long assignedBy;

    /** 关联状态 */
    private String status;
}
