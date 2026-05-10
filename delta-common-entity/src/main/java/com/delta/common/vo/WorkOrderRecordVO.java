package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工单记录视图对象")
public class WorkOrderRecordVO extends BaseVO {

    @Schema(description = "记录ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "记录类型", example = "HANDLE", allowableValues = {"CREATE", "ASSIGN", "HANDLE", "ESCALATE", "CLOSE"})
    private String recordType;

    @Schema(description = "操作人ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long operatorId;

    @Schema(description = "操作人名称", example = "客服小李")
    private String operatorName;

    @Schema(description = "操作人角色", example = "CS", allowableValues = {"ADMIN", "CS", "SYSTEM"})
    private String operatorRole;

    @Schema(description = "记录内容", example = "已联系用户确认问题")
    private String content;

    @Schema(description = "原状态", example = "PENDING", allowableValues = {"PENDING", "HANDLING", "RESOLVED", "CLOSED", "ESCALATED"})
    private String oldStatus;

    @Schema(description = "新状态", example = "HANDLING", allowableValues = {"PENDING", "HANDLING", "RESOLVED", "CLOSED", "ESCALATED"})
    private String newStatus;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
