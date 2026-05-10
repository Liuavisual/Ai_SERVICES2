package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工单视图对象")
public class WorkOrderVO extends BaseVO {

    @Schema(description = "工单ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "工单编号", example = "WO20260101000001")
    private String orderNo;

    @Schema(description = "工单类型", example = "COMPLAINT", allowableValues = {"COMPLAINT", "CONSULTATION", "REFUND", "TECHNICAL", "OTHER"})
    private String orderType;

    @Schema(description = "工单类型描述", example = "投诉")
    private String orderTypeDesc;

    @Schema(description = "优先级", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
    private String priority;

    @Schema(description = "优先级描述", example = "高")
    private String priorityDesc;

    @Schema(description = "来源平台", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    private String platform;

    @Schema(description = "来源平台描述", example = "微信")
    private String platformDesc;

    @Schema(description = "状态", example = "HANDLING", allowableValues = {"PENDING", "HANDLING", "RESOLVED", "CLOSED", "ESCALATED"})
    private String status;

    @Schema(description = "状态描述", example = "处理中")
    private String statusDesc;

    @Schema(description = "客户ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "客户名称", example = "小明")
    private String customerName;

    @Schema(description = "客户联系方式", example = "138****8000")
    private String customerContact;

    @Schema(description = "客户等级", example = "GOLD", allowableValues = {"NORMAL", "SILVER", "GOLD", "DIAMOND"})
    private String customerLevel;

    @Schema(description = "服务类型", example = "陪玩")
    private String serviceType;

    @Schema(description = "服务状态", example = "IN_PROGRESS", allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"})
    private String serviceStatus;

    @Schema(description = "问题详情", example = "陪玩师未按时上线")
    private String problemDetail;

    @Schema(description = "问题分类", example = "服务态度")
    private String problemCategory;

    @Schema(description = "触发关键词", example = "退款")
    private String triggerKeyword;

    @Schema(description = "上下文摘要", example = "用户对上次服务不满意，要求退款")
    private String contextSummary;

    @Schema(description = "分配的客服ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long assignedCsUserId;

    @Schema(description = "分配的客服名称", example = "客服小李")
    private String assignedCsName;

    @Schema(description = "处理人ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long handlerId;

    @Schema(description = "处理人名称", example = "客服小王")
    private String handlerName;

    @Schema(description = "处理结果", example = "已与用户协商解决")
    private String handleResult;

    @Schema(description = "处理截止时间", example = "2026-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    @Schema(description = "升级级别", example = "1")
    private Integer escalationLevel;

    @Schema(description = "提醒次数", example = "2")
    private Integer reminderCount;

    @Schema(description = "关联订单ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long relatedOrderId;

    @Schema(description = "关联陪玩师ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long relatedCompanionId;

    @Schema(description = "关联陪玩师名称", example = "小明同学")
    private String relatedCompanionName;

    @Schema(description = "满意度评分", example = "5")
    private Integer satisfactionScore;

    @Schema(description = "满意度备注", example = "服务态度很好")
    private String satisfactionRemark;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "解决时间", example = "2026-01-01 11:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;

    @Schema(description = "关闭时间", example = "2026-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedAt;

    @Schema(description = "工单记录列表")
    private List<WorkOrderRecordVO> records;

    @Schema(description = "工单附件列表")
    private List<WorkOrderAttachmentVO> attachments;

    @Schema(description = "服务跟踪信息")
    private ServiceTrackVO serviceTrack;
}
