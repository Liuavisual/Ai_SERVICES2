package com.delta.common.dto;

import com.delta.common.annotation.ObfuscatedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关键词数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "关键词数据传输对象")
public class KeywordDTO {

    @Schema(description = "关键词ID（支持混淆格式如d_xxxxx）", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "关键词内容", example = "退款")
    @NotBlank(message = "关键词不能为空")
    private String keyword;

    @Schema(description = "优先级", example = "10")
    private Integer priority = 0;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
