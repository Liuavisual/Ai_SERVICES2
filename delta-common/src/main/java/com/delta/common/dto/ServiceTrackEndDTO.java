package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务跟踪结束数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "服务跟踪结束数据传输对象")
public class ServiceTrackEndDTO {

    @Schema(description = "服务陪玩师ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long serviceCompanionId;

    @Schema(description = "服务陪玩师名称", example = "小明")
    private String serviceCompanionName;

    @Schema(description = "服务时长(分钟)", example = "120")
    private Integer serviceDuration;

    @Schema(description = "服务结果", example = "已完成")
    private String serviceResult;
}
