package com.delta.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderCreateDTO {

    @NotBlank(message = "工单类型不能为空")
    private String orderType;

    @NotBlank(message = "优先级不能为空")
    private String priority;

    @NotBlank(message = "来源平台不能为空")
    private String platform;

    @NotNull(message = "客户ID不能为空")
    private Long userId;

    private String serviceType;
    private String problemDetail;
    private String problemCategory;
    private String triggerKeyword;
    private String contextSummary;
    private Long relatedCompanionId;
    private Long assignedCsUserId;
}
