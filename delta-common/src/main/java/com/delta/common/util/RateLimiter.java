package com.delta.common.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 基于Redis Lua脚本的限流器，确保并发场景下的原子性
 *
 * @author 刘建国
 */
@Component
public class RateLimiter {

    /** Redis操作模板 */
    private final StringRedisTemplate redisTemplate;

    /** 限流Key前缀 */
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * Redis Lua脚本：原子性递增并检查限流
     * KEYS[1] = Redis Key
     * ARGV[1] = 最大请求数
     * ARGV[2] = 时间窗口（秒）
     * 返回值 1 表示允许通过，0 表示被限流
     */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
        RATE_LIMIT_SCRIPT.setScriptText(
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = redis.call('INCR', key) " +
            "if current == 1 then " +
            "    redis.call('EXPIRE', key, window) " +
            "end " +
            "if current > limit then " +
            "    return 0 " +
            "end " +
            "return 1"
        );
    }

    /**
     * 构造函数，注入RedisTemplate
     *
     * @param redisTemplate Redis字符串操作模板
     */
    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查请求是否允许通过限流
     * 使用Redis Lua脚本确保并发场景下的原子性，避免get-set之间的竞态条件
     *
     * @param key           限流标识Key
     * @param maxRequests   时间窗口内最大请求数
     * @param windowSeconds 时间窗口（秒）
     * @return true-允许通过，false-被限流
     */
    @SuppressWarnings("null")
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        List<String> keys = Collections.singletonList(redisKey);
        Long result = redisTemplate.execute(
            RATE_LIMIT_SCRIPT,
            keys,
            String.valueOf(maxRequests),
            String.valueOf(windowSeconds)
        );
        return result != null && result > 0;
    }
}
