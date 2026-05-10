package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工单创建数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "工单创建数据传输对象")
public class WorkOrderCreateDTO {

    @Schema(description = "工单类型", example = "COMPLAINT", allowableValues = {"COMPLAINT", "CONSULTATION", "REFUND", "TECHNICAL", "OTHER"})
    @NotBlank(message = "工单类型不能为空")
    private String orderType;

    @Schema(description = "优先级", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
    @NotBlank(message = "优先级不能为空")
    private String priority;

    @Schema(description = "来源平台", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    @NotBlank(message = "来源平台不能为空")
    private String platform;

    @Schema(description = "客户ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @NotNull(message = "客户ID不能为空")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "服务类型", example = "陪玩")
    private String serviceType;

    @Schema(description = "问题详情", example = "陪玩师未按时上线")
    private String problemDetail;

    @Schema(description = "问题分类", example = "服务态度")
    private String problemCategory;

    @Schema(description = "触发关键词", example = "退款")
    private String triggerKeyword;

    @Schema(description = "上下文摘要", example = "用户对上次服务不满意，要求退款")
    private String contextSummary;

    @Schema(description = "关联陪玩师ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long relatedCompanionId;

    @Schema(description = "分配的客服ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long assignedCsUserId;
}
