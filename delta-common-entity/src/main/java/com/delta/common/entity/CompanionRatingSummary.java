package com.delta.common.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 陪玩师综合评分汇总实体
 *
 * @author 刘建国
 */
@Data
public class CompanionRatingSummary {

    /** 主键ID */
    private Long id;

    /** 陪玩师ID */
    private Long companionId;

    /** 评价总数 */
    private Integer totalReviews;

    /** 平均评分(1.00-5.00) */
    private BigDecimal avgRating;

    /** 1星评价数 */
    private Integer rating1Count;

    /** 2星评价数 */
    private Integer rating2Count;

    /** 3星评价数 */
    private Integer rating3Count;

    /** 4星评价数 */
    private Integer rating4Count;

    /** 5星评价数 */
    private Integer rating5Count;

    /** 正面评价标签(逗号分隔) */
    private String positiveTags;

    /** 负面评价标签(逗号分隔) */
    private String negativeTags;

    /** 最近评价时间 */
    private LocalDateTime lastReviewAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}