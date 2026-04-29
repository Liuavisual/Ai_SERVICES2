package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 待处理消息处理数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "待处理消息处理数据传输对象")
public class PendingMessageHandleDTO {

    @Schema(description = "待处理消息ID", example = "1001")
    @NotNull(message = "待处理消息ID不能为空")
    /** 待处理消息ID */    private Long id;

    @Schema(description = "目标状态", example = "HANDLED", allowableValues = {"PENDING", "HANDLING", "HANDLED", "ESCALATED", "IGNORED"})
    @NotNull(message = "处理状态不能为空")
    /** 目标状态 */    private String status;

    @Schema(description = "处理备注", example = "已联系用户解决问题")
    /** 处理备注 */    private String remark;

    @Schema(description = "处理人ID", example = "3001")
    /** 处理人ID */    private Long handledBy;
}
