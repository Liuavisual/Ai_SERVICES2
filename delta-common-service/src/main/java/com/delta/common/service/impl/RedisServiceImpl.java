package com.delta.common.service.impl;

import com.delta.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务实现，封装RedisTemplate操作
 *
 * @author 刘建国
 */
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(@NonNull String key, @NonNull Object value) {
        Objects.requireNonNull(key, "Redis key不能为空");
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(@NonNull String key, @NonNull Object value, long timeout, @NonNull TimeUnit unit) {
        Objects.requireNonNull(key, "Redis key不能为空");
        Objects.requireNonNull(unit, "TimeUnit不能为空");
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public Boolean setIfAbsent(@NonNull String key, @NonNull Object value) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    @Override
    public Boolean setIfAbsent(@NonNull String key, @NonNull Object value, long timeout, @NonNull TimeUnit unit) {
        Objects.requireNonNull(key, "Redis key不能为空");
        Objects.requireNonNull(unit, "TimeUnit不能为空");
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    @Override
    public Object get(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public List<Object> multiGet(@NonNull Collection<String> keys) {
        Objects.requireNonNull(keys, "keys集合不能为空");
        return redisTemplate.opsForValue().multiGet(keys);
    }

    @Override
    public void multiSet(@NonNull Map<String, Object> map) {
        Objects.requireNonNull(map, "map不能为空");
        redisTemplate.opsForValue().multiSet(map);
    }

    @Override
    public Boolean delete(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.delete(key);
    }

    @Override
    public Long delete(@NonNull Collection<String> keys) {
        Objects.requireNonNull(keys, "keys集合不能为空");
        return redisTemplate.delete(keys);
    }

    @Override
    public Boolean hasKey(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.hasKey(key);
    }

    @Override
    public Boolean expire(@NonNull String key, long timeout, @NonNull TimeUnit unit) {
        Objects.requireNonNull(key, "Redis key不能为空");
        Objects.requireNonNull(unit, "TimeUnit不能为空");
        return redisTemplate.expire(key, timeout, unit);
    }

    @Override
    public Long getExpire(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.getExpire(key);
    }

    @Override
    public void hSet(@NonNull String key, @NonNull String hashKey, @NonNull Object value) {
        Objects.requireNonNull(key, "Redis key不能为空");
        Objects.requireNonNull(hashKey, "hashKey不能为空");
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public Object hGet(@NonNull String key, @NonNull String hashKey) {
        Objects.requireNonNull(key, "Redis key不能为空");
        Objects.requireNonNull(hashKey, "hashKey不能为空");
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    @Override
    public Map<Object, Object> hGetAll(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public void hSetAll(@NonNull String key, @NonNull Map<String, Object> map) {
        Objects.requireNonNull(key, "Redis key不能为空");
        Objects.requireNonNull(map, "map不能为空");
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public List<Object> hMultiGet(@NonNull String key, @NonNull Collection<String> hashKeys) {
        Objects.requireNonNull(key, "Redis key不能为空");
        Objects.requireNonNull(hashKeys, "hashKeys集合不能为空");
        return redisTemplate.opsForHash().multiGet(key, new ArrayList<>(hashKeys));
    }

    @Override
    public Boolean hHasKey(@NonNull String key, @NonNull String hashKey) {
        Objects.requireNonNull(key, "Redis key不能为空");
        Objects.requireNonNull(hashKey, "hashKey不能为空");
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Long hDelete(@NonNull String key, @NonNull Object... hashKeys) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    @Override
    public void lPush(@NonNull String key, @NonNull Object value) {
        Objects.requireNonNull(key, "Redis key不能为空");
        redisTemplate.opsForList().leftPush(key, value);
    }

    @Override
    public void rPush(@NonNull String key, @NonNull Object value) {
        Objects.requireNonNull(key, "Redis key不能为空");
        redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Object lPop(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public Object rPop(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForList().rightPop(key);
    }

    @Override
    public List<Object> lRange(@NonNull String key, long start, long end) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public Long lSize(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForList().size(key);
    }

    @Override
    public void sAdd(@NonNull String key, @NonNull Object... values) {
        Objects.requireNonNull(key, "Redis key不能为空");
        redisTemplate.opsForSet().add(key, values);
    }

    @Override
    public Set<Object> sMembers(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public Boolean sIsMember(@NonNull String key, @NonNull Object value) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForSet().isMember(key, value);
    }

    @Override
    public Long sSize(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Long sRemove(@NonNull String key, @NonNull Object... values) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForSet().remove(key, values);
    }

    @Override
    public void zAdd(@NonNull String key, @NonNull Object value, double score) {
        Objects.requireNonNull(key, "Redis key不能为空");
        redisTemplate.opsForZSet().add(key, value, score);
    }

    @Override
    public Set<Object> zRange(@NonNull String key, long start, long end) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    @Override
    public Set<Object> zReverseRange(@NonNull String key, long start, long end) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    @Override
    public Long zSize(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForZSet().size(key);
    }

    @Override
    public Long zRemove(@NonNull String key, @NonNull Object... values) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForZSet().remove(key, values);
    }

    @Override
    public void deleteByPattern(@NonNull String pattern) {
        Objects.requireNonNull(pattern, "pattern不能为空");
        Set<String> keys = scanKeys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public Set<String> scanKeys(@NonNull String pattern) {
        Objects.requireNonNull(pattern, "pattern不能为空");
        Set<String> keys = new LinkedHashSet<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(1000)
                .build();
        
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }

    @Override
    public Long increment(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Long increment(@NonNull String key, long delta) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long decrement(@NonNull String key) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForValue().decrement(key);
    }

    @Override
    public Long decrement(@NonNull String key, long delta) {
        Objects.requireNonNull(key, "Redis key不能为空");
        return redisTemplate.opsForValue().decrement(key, delta);
    }
}
