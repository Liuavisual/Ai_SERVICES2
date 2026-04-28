package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorkOrderVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    private String orderNo;
    private String orderType;
    private String orderTypeDesc;
    private String priority;
    private String priorityDesc;
    private String platform;
    private String platformDesc;
    private String status;
    private String statusDesc;
    @ObfuscatedId
    private Long userId;
    private String customerName;
    private String customerContact;
    private String customerLevel;
    private String serviceType;
    private String serviceStatus;
    private String problemDetail;
    private String problemCategory;
    private String triggerKeyword;
    private String contextSummary;
    @ObfuscatedId
    private Long assignedCsUserId;
    private String assignedCsName;
    @ObfuscatedId
    private Long handlerId;
    private String handlerName;
    private String handleResult;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    private Integer escalationLevel;
    private Integer reminderCount;
    @ObfuscatedId
    private Long relatedOrderId;
    @ObfuscatedId
    private Long relatedCompanionId;
    private String relatedCompanionName;
    private Integer satisfactionScore;
    private String satisfactionRemark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedAt;
    private List<WorkOrderRecordVO> records;
    private List<WorkOrderAttachmentVO> attachments;
    private ServiceTrackVO serviceTrack;
}
