package com.delta.platform.wechat.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WeChatConfig
 *
 * @author 刘建国
 */
@Configuration
@ConditionalOnProperty(prefix = "wx.mp", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WeChatConfig {

    @Bean
    @ConfigurationProperties(prefix = "wx.mp")
    public WxMpProperties wxMpProperties() {
        return new WxMpProperties();
    }

    @Data
    public static class WxMpProperties {
        private boolean enabled = false;
        private String appId;
        private String secret;
        private String token;
        private String aesKey;
    }
}
