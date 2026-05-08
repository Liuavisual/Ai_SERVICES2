package com.delta.common.service;

import java.util.Map;

/**
 * 缓存统计服务接口，监控缓存命中率和使用情况
 *
 * @author 刘建国
 */
public interface CacheStatsService {

    void recordHit(String cacheName);

    void recordMiss(String cacheName);

    Map<String, Object> getCacheStats(String cacheName);

    Map<String, Map<String, Object>> getAllCacheStats();

    void resetStats(String cacheName);

    void resetAllStats();
}
