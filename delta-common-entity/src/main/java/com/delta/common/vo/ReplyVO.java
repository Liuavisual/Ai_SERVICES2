package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "自动回复规则视图对象")
public class ReplyVO extends BaseVO {

    @Schema(description = "回复规则ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "触发类型", example = "KEYWORD", allowableValues = {"KEYWORD", "GREETING", "SCENE"})
    private String triggerType;

    @Schema(description = "触发键", example = "退款")
    private String triggerKey;

    @Schema(description = "回复内容", example = "您好，退款问题请提供订单号，我们会尽快处理。")
    private String content;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
