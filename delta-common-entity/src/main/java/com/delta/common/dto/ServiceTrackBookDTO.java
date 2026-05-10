package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务跟踪预约数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "服务跟踪预约数据传输对象")
public class ServiceTrackBookDTO {

    @Schema(description = "预约陪玩师ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long bookedCompanionId;

    @Schema(description = "预约陪玩师名称", example = "小明")
    private String bookedCompanionName;

    @Schema(description = "预约服务类型", example = "陪玩")
    private String bookedServiceType;

    @Schema(description = "预约时间段", example = "10:00-12:00")
    private String bookedTimeSlot;

    @Schema(description = "关联订单ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long relatedOrderId;
}
