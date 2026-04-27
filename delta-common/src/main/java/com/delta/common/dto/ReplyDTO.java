package com.delta.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 自动回复规则数据传输对象
 *
 * @author delta
 */
@Data
public class ReplyDTO {

    private Long id;

    @NotBlank(message = "触发类型不能为空")
    /** 触发类型 */    private String triggerType;

    /** 触发键 */    private String triggerKey;

    @NotBlank(message = "回复内容不能为空")
    /** 回复内容 */    private String content;

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
