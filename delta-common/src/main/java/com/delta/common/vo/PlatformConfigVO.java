package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.delta.common.util.DesensitizeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlatformConfigVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    private String platform;
    private Boolean enabled;
    private Map<String, Object> config;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

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
