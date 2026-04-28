package com.delta.common.vo;

import com.delta.common.annotation.ObfuscatedId;
import com.delta.common.util.DesensitizeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiConfigVO extends BaseVO {

    @ObfuscatedId
    private Long id;
    private String configKey;
    private String configType;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private String configValueRaw;
    private String configValue;

    public String getConfigValue() {
        if (configValueRaw != null && DesensitizeUtils.isSensitiveKey(configKey)) {
            return DesensitizeUtils.maskSecret(configValueRaw);
        }
        return configValueRaw;
    }

    public void setConfigValue(String configValue) {
        this.configValueRaw = configValue;
    }
}
