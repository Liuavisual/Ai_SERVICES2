package com.delta.platform.wework.config;

import com.delta.platform.wework.crypto.WeWorkCryptoUtils;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConditionalOnProperty(prefix = "wework", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "wework")
public class WeWorkConfig {

    private boolean enabled = false;
    private String corpId;
    private Integer agentId;
    private String appSecret;
    private String contactSecret;
    private String callbackToken;
    private String callbackEncodingAESKey;
    private String senderUserId;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WeWorkCryptoUtils weWorkCryptoUtils() {
        return new WeWorkCryptoUtils(callbackToken, callbackEncodingAESKey);
    }
}
