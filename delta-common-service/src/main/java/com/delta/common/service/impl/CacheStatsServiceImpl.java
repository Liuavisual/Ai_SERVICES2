package com.delta.common.service.impl;

import com.delta.common.service.CacheStatsService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存统计服务实现，监控缓存性能
 *
 * @author 刘建国
 */
@Service
public class CacheStatsServiceImpl implements CacheStatsService {

    private static class CacheStats {
        final AtomicLong hits = new AtomicLong(0);
        final AtomicLong misses = new AtomicLong(0);
        final long startTime = System.currentTimeMillis();

        double getHitRate() {
            long total = hits.get() + misses.get();
            return total == 0 ? 0 : (double) hits.get() / total;
        }
    }

    private final Map<String, CacheStats> statsMap = new ConcurrentHashMap<>();

    private CacheStats getOrCreateStats(String cacheName) {
        return statsMap.computeIfAbsent(cacheName, k -> new CacheStats());
    }

    @Override
    public void recordHit(String cacheName) {
        getOrCreateStats(cacheName).hits.incrementAndGet();
    }

    @Override
    public void recordMiss(String cacheName) {
        getOrCreateStats(cacheName).misses.incrementAndGet();
    }

    @Override
    public Map<String, Object> getCacheStats(String cacheName) {
        CacheStats stats = statsMap.get(cacheName);
        if (stats == null) {
            return Map.of();
        }
        return Map.of(
                "cacheName", cacheName,
                "hits", stats.hits.get(),
                "misses", stats.misses.get(),
                "hitRate", String.format("%.2f%%", stats.getHitRate() * 100),
                "totalRequests", stats.hits.get() + stats.misses.get(),
                "uptimeSeconds", (System.currentTimeMillis() - stats.startTime) / 1000
        );
    }

    @Override
    public Map<String, Map<String, Object>> getAllCacheStats() {
        Map<String, Map<String, Object>> result = new ConcurrentHashMap<>();
        for (String cacheName : statsMap.keySet()) {
            result.put(cacheName, getCacheStats(cacheName));
        }
        return result;
    }

    @Override
    public void resetStats(String cacheName) {
        statsMap.remove(cacheName);
    }

    @Override
    public void resetAllStats() {
        statsMap.clear();
    }
}
