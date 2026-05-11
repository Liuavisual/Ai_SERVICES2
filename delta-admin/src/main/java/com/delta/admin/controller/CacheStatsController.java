package com.delta.admin.controller;

import com.delta.common.annotation.PermAuth;
import com.delta.common.constant.ApiVersionConstants;
import com.delta.common.service.CacheStatsService;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 缓存统计控制器
 * <p>
 * 权限：仅 SYS_ADMIN
 * </p>
 *
 * @author 刘建国
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiVersionConstants.V1 + "/cache-stats")
@Tag(name = "缓存统计", description = "缓存统计相关接口")
@PermAuth("cache:view")
public class CacheStatsController {

    private final CacheStatsService cacheStatsService;

    @GetMapping("/{cacheName}")
    @Operation(summary = "获取指定缓存的统计信息")
    public Result<Map<String, Object>> getCacheStats(@PathVariable("cacheName") String cacheName) {
        return Result.success(cacheStatsService.getCacheStats(cacheName));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有缓存的统计信息")
    public Result<Map<String, Map<String, Object>>> getAllCacheStats() {
        return Result.success(cacheStatsService.getAllCacheStats());
    }

    @DeleteMapping("/{cacheName}")
    @Operation(summary = "重置指定缓存的统计信息")
    @PermAuth("cache:edit")
    public Result<Void> resetStats(@PathVariable("cacheName") String cacheName) {
        cacheStatsService.resetStats(cacheName);
        return Result.success();
    }

    @DeleteMapping("/all")
    @Operation(summary = "重置所有缓存的统计信息")
    public Result<Void> resetAllStats() {
        cacheStatsService.resetAllStats();
        return Result.success();
    }
}
