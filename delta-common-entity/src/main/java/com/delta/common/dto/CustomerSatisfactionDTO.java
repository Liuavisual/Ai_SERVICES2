package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 客户满意度评价数据传输对象
 *
 * @author 刘建国
 */
@Schema(description = "客户满意度评价DTO")
public class CustomerSatisfactionDTO {

    /** 服务追踪ID（混淆后的ID） */
    @NotNull(message = "服务追踪ID不能为空")
    @Schema(description = "服务追踪ID", example = "d_xxxxx")
    private String serviceTrackId;

    /** 评分（1-5） */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    @Schema(description = "评分(1-5)", example = "5")
    private Integer rating;

    /** 反馈内容 */
    @Schema(description = "反馈内容", example = "服务很好，态度专业")
    private String feedback;

    /** 服务类型 */
    @Schema(description = "服务类型", example = "陪玩")
    private String serviceType;

    /** 标签（逗号分隔） */
    @Schema(description = "标签(逗号分隔)", example = "态度好,专业,耐心")
    private String tags;

    /** 是否匿名（0-否，1-是） */
    @Schema(description = "是否匿名(0-否,1-是)", example = "0")
    private Integer isAnonymous;

    /**
     * 获取服务追踪ID
     * @return 服务追踪ID
     */
    public String getServiceTrackId() { return serviceTrackId; }

    /**
     * 设置服务追踪ID
     * @param serviceTrackId 服务追踪ID
     */
    public void setServiceTrackId(String serviceTrackId) { this.serviceTrackId = serviceTrackId; }

    /**
     * 获取评分
     * @return 评分
     */
    public Integer getRating() { return rating; }

    /**
     * 设置评分
     * @param rating 评分
     */
    public void setRating(Integer rating) { this.rating = rating; }

    /**
     * 获取反馈内容
     * @return 反馈内容
     */
    public String getFeedback() { return feedback; }

    /**
     * 设置反馈内容
     * @param feedback 反馈内容
     */
    public void setFeedback(String feedback) { this.feedback = feedback; }

    /**
     * 获取服务类型
     * @return 服务类型
     */
    public String getServiceType() { return serviceType; }

    /**
     * 设置服务类型
     * @param serviceType 服务类型
     */
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    /**
     * 获取标签
     * @return 标签
     */
    public String getTags() { return tags; }

    /**
     * 设置标签
     * @param tags 标签
     */
    public void setTags(String tags) { this.tags = tags; }

    /**
     * 获取是否匿名
     * @return 是否匿名
     */
    public Integer getIsAnonymous() { return isAnonymous; }

    /**
     * 设置是否匿名
     * @param isAnonymous 是否匿名
     */
    public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }
}
