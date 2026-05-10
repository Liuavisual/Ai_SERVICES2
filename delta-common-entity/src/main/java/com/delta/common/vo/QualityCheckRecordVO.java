package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "质检记录视图对象")
public class QualityCheckRecordVO extends BaseVO {

    @Schema(description = "质检记录ID")
    @ObfuscatedId
    private Long id;

    @Schema(description = "订单ID")
    @ObfuscatedId
    private Long orderId;

    @Schema(description = "陪玩师ID")
    @ObfuscatedId
    private Long companionId;

    @Schema(description = "陪玩师昵称")
    private String companionNickname;

    @Schema(description = "客户ID")
    @ObfuscatedId
    private Long userId;

    @Schema(description = "客户名称")
    private String userName;

    @Schema(description = "检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    @Schema(description = "检测类型")
    private String checkType;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "检测得分")
    private Integer score;

    @Schema(description = "违规类型")
    private String violationType;

    @Schema(description = "违规内容摘要")
    private String violationSummary;

    @Schema(description = "证据URL")
    private String evidenceUrl;

    @Schema(description = "处理建议")
    private String actionSuggestion;

    @Schema(description = "处理状态")
    private String handleStatus;

    @Schema(description = "处理人ID")
    @ObfuscatedId
    private Long handlerId;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
