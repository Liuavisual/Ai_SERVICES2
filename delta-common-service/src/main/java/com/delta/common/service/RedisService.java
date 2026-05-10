package com.delta.common.service;

import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务接口，提供键值操作、过期管理等功能
 *
 * @author 刘建国
 */
public interface RedisService {

    void set(@NonNull String key, @NonNull Object value);

    void set(@NonNull String key, @NonNull Object value, long timeout, @NonNull TimeUnit unit);

    Boolean setIfAbsent(@NonNull String key, @NonNull Object value);

    Boolean setIfAbsent(@NonNull String key, @NonNull Object value, long timeout, @NonNull TimeUnit unit);

    Object get(@NonNull String key);

    List<Object> multiGet(@NonNull Collection<String> keys);

    void multiSet(@NonNull Map<String, Object> map);

    Boolean delete(@NonNull String key);

    Long delete(@NonNull Collection<String> keys);

    Boolean hasKey(@NonNull String key);

    Boolean expire(@NonNull String key, long timeout, @NonNull TimeUnit unit);

    Long getExpire(@NonNull String key);

    void hSet(@NonNull String key, @NonNull String hashKey, @NonNull Object value);

    Object hGet(@NonNull String key, @NonNull String hashKey);

    Map<Object, Object> hGetAll(@NonNull String key);

    void hSetAll(@NonNull String key, @NonNull Map<String, Object> map);

    List<Object> hMultiGet(@NonNull String key, @NonNull Collection<String> hashKeys);

    Boolean hHasKey(@NonNull String key, @NonNull String hashKey);

    Long hDelete(@NonNull String key, @NonNull Object... hashKeys);

    void lPush(@NonNull String key, @NonNull Object value);

    void rPush(@NonNull String key, @NonNull Object value);

    Object lPop(@NonNull String key);

    Object rPop(@NonNull String key);

    List<Object> lRange(@NonNull String key, long start, long end);

    Long lSize(@NonNull String key);

    void sAdd(@NonNull String key, @NonNull Object... values);

    Set<Object> sMembers(@NonNull String key);

    Boolean sIsMember(@NonNull String key, @NonNull Object value);

    Long sSize(@NonNull String key);

    Long sRemove(@NonNull String key, @NonNull Object... values);

    void zAdd(@NonNull String key, @NonNull Object value, double score);

    Set<Object> zRange(@NonNull String key, long start, long end);

    Set<Object> zReverseRange(@NonNull String key, long start, long end);

    Long zSize(@NonNull String key);

    Long zRemove(@NonNull String key, @NonNull Object... values);

    void deleteByPattern(@NonNull String pattern);

    Set<String> scanKeys(@NonNull String pattern);

    Long increment(@NonNull String key);

    Long increment(@NonNull String key, long delta);

    Long decrement(@NonNull String key);

    Long decrement(@NonNull String key, long delta);
}
