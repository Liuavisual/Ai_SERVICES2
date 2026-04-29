package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 自动回复规则数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "自动回复规则数据传输对象")
public class ReplyDTO {

    @Schema(description = "回复规则ID", example = "1")
    private Long id;

    @Schema(description = "触发类型", example = "KEYWORD", allowableValues = {"KEYWORD", "GREETING", "SCENE"})
    @NotBlank(message = "触发类型不能为空")
    /** 触发类型 */    private String triggerType;

    @Schema(description = "触发键", example = "退款")
    /** 触发键 */    private String triggerKey;

    @Schema(description = "回复内容", example = "您好，退款问题请提供订单号，我们会尽快处理。")
    @NotBlank(message = "回复内容不能为空")
    /** 回复内容 */    private String content;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    /** 是否启用 */    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerKey() {
        return triggerKey;
    }

    public void setTriggerKey(String triggerKey) {
        this.triggerKey = triggerKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
