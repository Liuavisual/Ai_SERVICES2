package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关键词数据传输对象
 *
 * @author delta
 */
@Data
@Schema(description = "关键词数据传输对象")
public class KeywordDTO {

    @Schema(description = "关键词ID", example = "1")
    private Long id;

    @Schema(description = "关键词内容", example = "退款")
    @NotBlank(message = "关键词不能为空")
    /** 关键词内容 */    private String keyword;

    @Schema(description = "优先级", example = "10")
    /** 优先级 */    private Integer priority = 0;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    /** 是否启用 */    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
