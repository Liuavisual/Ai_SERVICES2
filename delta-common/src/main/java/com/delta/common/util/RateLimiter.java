package com.delta.common.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        String countStr = redisTemplate.opsForValue().get(redisKey);
        if (countStr == null) {
            redisTemplate.opsForValue().set(redisKey, "1", windowSeconds, TimeUnit.SECONDS);
            return true;
        }
        int count = Integer.parseInt(countStr);
        if (count >= maxRequests) {
            return false;
        }
        redisTemplate.opsForValue().increment(redisKey);
        return true;
    }
}
