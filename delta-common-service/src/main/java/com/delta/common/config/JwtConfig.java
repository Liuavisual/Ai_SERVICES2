package com.delta.common.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    private String secret;

    private Long expiration = 900000L;

    private Long refreshExpiration = 604800000L;

    private String issuer = "delta-ai-customer-service";

    private String audience = "delta-admin";

    private boolean enabled = false;

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.isEmpty()) {
            if (enabled) {
                throw new IllegalStateException(
                        "[安全错误] JWT密钥未配置！"
                                + "请在application.yml中配置 jwt.secret 或设置环境变量 JWT_SECRET");
            }
            log.warn("[安全警告] JWT密钥未配置，JWT认证已禁用。请在生产环境中通过环境变量 JWT_SECRET 配置强密钥");
            return;
        }
        if (enabled && secret.length() < 32) {
            throw new IllegalStateException(
                    "[安全错误] JWT密钥长度不足32字符，当前长度: " + secret.length()
                            + "，请使用至少32字符的强密钥");
        }
        if (enabled) {
            log.info("JWT密钥配置验证通过，密钥长度: {}字符", secret.length());
        }
    }
}
