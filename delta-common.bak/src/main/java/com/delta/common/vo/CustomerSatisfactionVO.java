package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客户满意度评价视图对象
 *
 * @author 刘建国
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户满意度评价")
public class CustomerSatisfactionVO extends BaseVO {

    /** 评价ID */
    @ObfuscatedId
    @Schema(description = "评价ID", example = "d_xxxxx")
    private Long id;

    /** 客户ID */
    @ObfuscatedId
    @Schema(description = "客户ID", example = "d_xxxxx")
    private Long userId;

    /** 客户昵称 */
    @Schema(description = "客户昵称", example = "张三")
    private String userNickname;

    /** 服务追踪ID */
    @ObfuscatedId
    @Schema(description = "服务追踪ID", example = "d_xxxxx")
    private Long serviceTrackId;

    /** 陪玩师ID */
    @ObfuscatedId
    @Schema(description = "陪玩师ID", example = "d_xxxxx")
    private Long companionId;

    /** 陪玩师昵称 */
    @Schema(description = "陪玩师昵称", example = "小李")
    private String companionName;

    /** 评分（1-5） */
    @Schema(description = "评分(1-5)", example = "5")
    private Integer rating;

    /** 反馈内容 */
    @Schema(description = "反馈内容", example = "服务很好")
    private String feedback;

    /** 服务类型 */
    @Schema(description = "服务类型", example = "陪玩")
    private String serviceType;

    /** 标签 */
    @Schema(description = "标签", example = "态度好,专业")
    private String tags;

    /** 是否匿名 */
    @Schema(description = "是否匿名", example = "0")
    private Integer isAnonymous;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createdAt;
}
