package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "工单确认数据传输对象")
public class WorkOrderConfirmDTO {

    @Schema(description = "满意度评分", example = "5")
    @NotNull(message = "满意度评分不能为空")
    private Integer satisfactionScore;

    @Schema(description = "满意度备注", example = "服务态度很好，问题解决及时")
    private String satisfactionRemark;
}
