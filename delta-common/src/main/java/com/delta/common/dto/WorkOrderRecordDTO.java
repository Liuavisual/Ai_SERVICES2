package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "工单记录数据传输对象")
public class WorkOrderRecordDTO {

    @Schema(description = "记录类型", example = "HANDLE", allowableValues = {"CREATE", "ASSIGN", "HANDLE", "ESCALATE", "CLOSE"})
    @NotBlank(message = "记录类型不能为空")
    private String recordType;

    @Schema(description = "记录内容", example = "已联系用户确认问题")
    @NotBlank(message = "记录内容不能为空")
    private String content;
}
