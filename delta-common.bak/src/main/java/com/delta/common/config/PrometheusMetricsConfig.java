package com.delta.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prometheus性能监控配置
 * 集成Micrometer + Prometheus，暴露以下监控指标：
 * - JVM内存、GC
 * - API响应时间、请求量
 * - 数据库连接池状态
 * - Redis命中率
 *
 * @author 刘建国
 */
@Configuration
public class PrometheusMetricsConfig {

    private static final Logger log = LoggerFactory.getLogger(PrometheusMetricsConfig.class);

    /**
     * 注册全局监控指标标签
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        log.info("【Prometheus】初始化性能监控端点");
        return registry -> registry.config().commonTags(
                "application", "delta-ai-customer-service",
                "version", "1.0.0",
                "java_version", System.getProperty("java.version")
        );
    }
}