package com.delta.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * AI配置更新数据传输对象
 *
 * @author 刘建国
 */
@Data
@Schema(description = "AI配置更新数据传输对象")
public class AiConfigUpdateDTO {

    @Schema(description = "待更新的配置项列表")
    @NotEmpty(message = "配置项不能为空")
    /** 待更新的配置项列表 */
    private List<ConfigUpdateItem> updates;

    public List<ConfigUpdateItem> getUpdates() {
        return updates;
    }

    public void setUpdates(List<ConfigUpdateItem> updates) {
        this.updates = updates;
    }

    @Data
    @Schema(description = "配置项更新条目")
    public static class ConfigUpdateItem {

        @Schema(description = "配置键", example = "ai_model_name")
        private String configKey;

        @Schema(description = "配置值", example = "gpt-4")
        private String configValue;

        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigValue() {
            return configValue;
        }

        public void setConfigValue(String configValue) {
            this.configValue = configValue;
        }
    }
}
