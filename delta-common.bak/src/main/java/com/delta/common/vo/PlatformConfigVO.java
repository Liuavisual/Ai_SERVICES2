package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.delta.common.util.DesensitizeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "平台配置视图对象")
public class PlatformConfigVO extends BaseVO {

    @Schema(description = "配置ID", example = "d_xxxxx")
    @ObfuscatedId
    private Long id;

    @Schema(description = "平台标识", example = "WECHAT", allowableValues = {"WECHAT", "WEWORK", "APP", "WEB"})
    private String platform;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "平台配置项(JSON格式，敏感字段已脱敏)", example = "{\"appId\":\"wx1234\",\"appSecret\":\"****\"}")
    private Map<String, Object> config;

    @Schema(description = "创建时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public Map<String, Object> getConfig() {
        if (config != null) {
            config.forEach((key, value) -> {
                if (value instanceof String && DesensitizeUtils.isSensitiveKey(key)) {
                    config.put(key, DesensitizeUtils.maskSecret((String) value));
                }
            });
        }
        return config;
    }
}
