package com.delta.common.service.impl;

import com.delta.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final RedisService redisService;

    public void blacklistToken(String token, long remainingMillis) {
        if (token == null || token.isEmpty()) {
            return;
        }
        String key = BLACKLIST_PREFIX + token;
        long ttl = Math.max(remainingMillis / 1000, 1);
        redisService.set(key, "1", ttl, TimeUnit.SECONDS);
        log.info("Token已加入黑名单, TTL={}秒", ttl);
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisService.hasKey(key));
    }
}
