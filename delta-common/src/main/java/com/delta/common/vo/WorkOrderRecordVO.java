package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorkOrderRecordVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    private String recordType;
    @ObfuscatedId
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String content;
    private String oldStatus;
    private String newStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
