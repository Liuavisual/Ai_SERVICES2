package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客服-客户关联实体
 * <p>
 * 对应数据库表 cs_user_customer，记录客服与客户之间的分配关系，
 * 包括分配类型、分配时间、分配人、状态等。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cs_user_customer")
@Table(name = "cs_user_customer", indexes = {
        @Index(name = "idx_cs_user_customer_cs_user_id", columnList = "cs_user_id"),
        @Index(name = "idx_cs_user_customer_customer_user_id", columnList = "customer_user_id"),
        @Index(name = "idx_cs_user_customer_status", columnList = "status"),
        @Index(name = "idx_cs_user_customer_cs_user_status", columnList = "cs_user_id,status"),
        @Index(name = "idx_cs_user_customer_customer_status", columnList = "customer_user_id,status")
})
public class CsUserCustomer extends BaseEntity {

    /** 客服用户ID */
    private Long csUserId;

    /** 客户用户ID */
    private Long customerUserId;

    /** 分配类型 */
    private String assignType;

    /** 分配时间 */
    private LocalDateTime assignedAt;

    /** 分配操作人ID */
    private Long assignedBy;

    /** 关联状态 */
    private String status;
}
