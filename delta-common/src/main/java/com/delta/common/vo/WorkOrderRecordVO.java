package com.delta.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkOrderRecordVO {

    private Long id;
    private String recordType;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String content;
    private String oldStatus;
    private String newStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
