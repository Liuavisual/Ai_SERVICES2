package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 自动回复规则数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "自动回复规则数据传输对象")
public class ReplyDTO {

    @Schema(description = "回复规则ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "触发类型", example = "KEYWORD", allowableValues = {"KEYWORD", "GREETING", "SCENE"})
    @NotBlank(message = "触发类型不能为空")
    private String triggerType;

    @Schema(description = "触发键", example = "退款")
    private String triggerKey;

    @Schema(description = "回复内容", example = "您好，退款问题请提供订单号，我们会尽快处理。")
    @NotBlank(message = "回复内容不能为空")
    private String content;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
