package com.delta.common.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * AI配置更新数据传输对象
 *
 * @author delta
 */
@Data
public class AiConfigUpdateDTO {

    @NotEmpty(message = "配置项不能为空")
    /** 待更新的配置项列表 */    private List<ConfigUpdateItem> updates;

    public List<ConfigUpdateItem> getUpdates() {
        return updates;
    }

    public void setUpdates(List<ConfigUpdateItem> updates) {
        this.updates = updates;
    }

    @Data
    public static class ConfigUpdateItem {
        private String configKey;
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
