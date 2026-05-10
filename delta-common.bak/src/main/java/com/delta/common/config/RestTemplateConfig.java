package com.delta.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * 为 delta-common 模块提供默认的 RestTemplate Bean，
 * 供 AI 模型服务等组件自动注入使用
 *
 * @author 刘建国
 */
@Configuration
public class RestTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);

    /**
     * 创建默认 RestTemplate Bean
     * 仅在容器中不存在同名 Bean 时才创建，避免与子模块冲突
     *
     * @return RestTemplate 实例
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        log.info("【RestTemplate配置】创建默认 RestTemplate 实例");
        return new RestTemplate();
    }
}