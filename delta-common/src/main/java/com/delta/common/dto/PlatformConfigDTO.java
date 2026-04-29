package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "平台配置数据传输对象")
public class PlatformConfigDTO {

    @Schema(description = "配置ID", example = "1")
    private Long id;

    @Schema(description = "平台标识", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    @NotBlank(message = "平台不能为空")
    /** 平台标识 */    private String platform;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    /** 是否启用 */    private Boolean enabled;

    @Schema(description = "平台配置项(JSON格式)", example = "{\"appId\":\"wx1234\",\"appSecret\":\"****\"}")
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
