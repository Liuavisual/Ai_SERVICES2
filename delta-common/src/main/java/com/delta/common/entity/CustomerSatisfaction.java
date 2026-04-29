package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

/**
 * 客户满意度评价实体
 * <p>
 * 对应数据库表 customer_satisfaction，记录客户对服务完成的满意度评价，
 * 包括评分、反馈内容、服务类型、标签等信息。</p>
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_satisfaction")
@Table(name = "customer_satisfaction", indexes = {
        @Index(name = "idx_satisfaction_user_id", columnList = "user_id"),
        @Index(name = "idx_satisfaction_service_track_id", columnList = "service_track_id"),
        @Index(name = "idx_satisfaction_companion_id", columnList = "companion_id"),
        @Index(name = "idx_satisfaction_rating", columnList = "rating"),
        @Index(name = "idx_satisfaction_created_at", columnList = "created_at")
})
public class CustomerSatisfaction extends BaseEntity {

    /** 客户ID */
    private Long userId;

    /** 服务追踪ID */
    private Long serviceTrackId;

    /** 陪玩师ID */
    private Long companionId;

    /** 评分（1-5） */
    private Integer rating;

    /** 反馈内容 */
    @TableField(value = "feedback", jdbcType = JdbcType.LONGVARCHAR)
    private String feedback;

    /** 服务类型 */
    private String serviceType;

    /** 标签（逗号分隔） */
    private String tags;

    /** 是否匿名：0-否，1-是 */
    @TableField("is_anonymous")
    private Integer isAnonymous;
}
