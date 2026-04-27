package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客服-客户分配关系实体
 * <p>
 * 对应数据库表 cs_user_customer，记录客服人员与客户之间的分配关系，
 * 支持手动分配（MANUAL）和系统自动分配（SYSTEM）两种方式。</p>
 *
 * @author delta
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cs_user_customer")
public class CsUserCustomer extends BaseEntity {

    /** 客服人员ID（关联sys_user表） */
    private Long csUserId;

    /** 客户ID（关联users表） */
    private Long customerUserId;

    /** 分配方式：MANUAL-手动分配，SYSTEM-系统自动分配 */
    private String assignType;

    /** 分配时间 */
    private LocalDateTime assignedAt;

    /** 执行分配的操作人ID */
    private Long assignedBy;

    /** 状态：ACTIVE-生效中，INACTIVE-已失效 */
    private String status;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
