package com.delta.admin.listener;

import com.delta.common.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 缓存初始化监听器，应用启动时预加载关键词和回复规则到缓存
 *
 * @author delta
 */
@Component
@RequiredArgsConstructor
public class CacheInitListener {

    private static final Logger log = LoggerFactory.getLogger(CacheInitListener.class);

    private final CacheService cacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("========================================");
        log.info("应用启动完成，开始初始化缓存...");
        log.info("========================================");

        try {
            cacheService.initAllCaches();
            
            log.info("========================================");
            log.info("缓存初始化完成！");
            log.info("========================================");
        } catch (Exception e) {
            log.error("缓存初始化失败", e);
        }
    }
}
