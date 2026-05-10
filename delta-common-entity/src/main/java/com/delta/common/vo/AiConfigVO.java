package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI配置视图对象")
public class AiConfigVO extends BaseVO {

    @Schema(description = "配置ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "配置键", example = "ai_model_name")
    private String configKey;

    @Schema(description = "配置类型", example = "STRING", allowableValues = {"STRING", "NUMBER", "BOOLEAN", "JSON"})
    private String configType;

    @Schema(description = "配置描述", example = "AI模型名称")
    private String description;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String configValue;
}
