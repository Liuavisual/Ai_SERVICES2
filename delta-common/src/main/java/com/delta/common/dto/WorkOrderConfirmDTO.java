package com.delta.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderConfirmDTO {

    @NotNull(message = "满意度评分不能为空")
    private Integer satisfactionScore;

    private String satisfactionRemark;
}
