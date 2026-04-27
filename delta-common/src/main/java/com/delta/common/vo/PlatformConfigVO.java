package com.delta.common.vo;

import com.delta.common.util.DesensitizeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class PlatformConfigVO {

    private Long id;
    private String platform;
    private Boolean enabled;
    private Map<String, Object> config;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public Map<String, Object> getConfig() {
        if (config == null) {
            return null;
        }
        Map<String, Object> maskedConfig = new HashMap<>();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String && DesensitizeUtils.isSensitiveKey(key)) {
                maskedConfig.put(key, DesensitizeUtils.maskSecret((String) value));
            } else {
                maskedConfig.put(key, value);
            }
        }
        return maskedConfig;
    }
}
