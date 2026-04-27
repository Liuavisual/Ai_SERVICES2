package com.delta.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

/**
 * 平台配置数据传输对象
 *
 * @author delta
 */
@Data
public class PlatformConfigDTO {

    private Long id;

    @NotBlank(message = "平台不能为空")
    /** 平台标识 */    private String platform;

    @NotNull(message = "启用状态不能为空")
    /** 是否启用 */    private Boolean enabled;

    private Map<String, Object> config;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
