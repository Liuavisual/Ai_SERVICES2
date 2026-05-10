package com.delta.common.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AI模型健康检查指示器
 * 监控所有注册的AI模型可用性，输出到Actuator /health端点
 *
 * @author 刘建国
 */
@Component
public class AiModelHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(AiModelHealthIndicator.class);

    private final List<AiModelService> aiModelServices;

    public AiModelHealthIndicator(List<AiModelService> aiModelServices) {
        this.aiModelServices = aiModelServices;
    }

    @Override
    public Health health() {
        if (aiModelServices == null || aiModelServices.isEmpty()) {
            return Health.down()
                    .withDetail("message", "没有找到已注册的AI模型服务")
                    .build();
        }

        Health.Builder builder = Health.up();
        boolean allAvailable = true;

        for (AiModelService service : aiModelServices) {
            boolean available = service.isAvailable();
            if (!available) {
                allAvailable = false;
                log.warn("【AI健康检查】模型 {} 不可用", service.getModelName());
            }
            builder.withDetail(service.getModelName(), Map.of(
                    "available", available,
                    "model", service.getModelName()
            ));
        }

        return allAvailable ? builder.build() : Health.down().withDetail("message", "部分AI模型不可用").build();
    }
}