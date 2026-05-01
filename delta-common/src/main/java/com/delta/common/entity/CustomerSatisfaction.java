package com.delta.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_satisfaction")
public class CustomerSatisfaction extends BaseEntity {

    /** 客户ID */
    private Long userId;

    /** 关联订单ID */
    @TableField("related_order_id")
    private Long serviceTrackId;

    /** 陪玩师ID */
    @TableField("related_companion_id")
    private Long companionId;

    /** 评分（1-5） */
    @TableField("satisfaction_score")
    private Integer rating;

    /** 反馈内容 */
    @TableField("comment")
    private String feedback;

    /** 标签（逗号分隔） */
    @TableField("feedback_tags")
    private String tags;

    /** 是否匿名：0-否，1-是 */
    @TableField("is_anonymous")
    private Integer isAnonymous;

    /** 服务类型 */
    private String serviceType;

    /** 回复内容 */
    @TableField("reply_content")
    private String replyContent;

    /** 回复人ID */
    @TableField("replied_by")
    private Long repliedBy;

    /** 回复时间 */
    @TableField("replied_at")
    private LocalDateTime repliedAt;
}
