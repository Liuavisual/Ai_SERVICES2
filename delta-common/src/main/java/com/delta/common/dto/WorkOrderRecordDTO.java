package com.delta.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkOrderRecordDTO {

    @NotBlank(message = "记录类型不能为空")
    private String recordType;

    @NotBlank(message = "记录内容不能为空")
    private String content;
}
