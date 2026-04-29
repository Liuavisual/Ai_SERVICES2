package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "工单提交数据传输对象")
public class WorkOrderSubmitDTO {

    @Schema(description = "处理结果", example = "已与用户协商解决，用户满意")
    @NotBlank(message = "处理结果不能为空")
    private String handleResult;
}
