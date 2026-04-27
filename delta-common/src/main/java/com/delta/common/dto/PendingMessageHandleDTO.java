package com.delta.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 待处理消息处理数据传输对象
 *
 * @author delta
 */
@Data
public class PendingMessageHandleDTO {

    @NotNull(message = "待处理消息ID不能为空")
    /** 待处理消息ID */    private Long id;

    @NotNull(message = "处理状态不能为空")
    /** 目标状态 */    private String status;

    /** 处理备注 */    private String remark;

    /** 处理人ID */    private Long handledBy;
}
