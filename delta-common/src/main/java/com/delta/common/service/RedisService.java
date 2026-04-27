package com.delta.common.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务接口，提供键值操作、过期管理等功能
 *
 * @author delta
 */
public interface RedisService {

    void set(String key, Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    Boolean setIfAbsent(String key, Object value);

    Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit);

    Object get(String key);

    List<Object> multiGet(Collection<String> keys);

    void multiSet(Map<String, Object> map);

    Boolean delete(String key);

    Long delete(Collection<String> keys);

    Boolean hasKey(String key);

    Boolean expire(String key, long timeout, TimeUnit unit);

    Long getExpire(String key);

    void hSet(String key, String hashKey, Object value);

    Object hGet(String key, String hashKey);

    Map<Object, Object> hGetAll(String key);

    void hSetAll(String key, Map<String, Object> map);

    List<Object> hMultiGet(String key, Collection<String> hashKeys);

    Boolean hHasKey(String key, String hashKey);

    Long hDelete(String key, Object... hashKeys);

    void lPush(String key, Object value);

    void rPush(String key, Object value);

    Object lPop(String key);

    Object rPop(String key);

    List<Object> lRange(String key, long start, long end);

    Long lSize(String key);

    void sAdd(String key, Object... values);

    Set<Object> sMembers(String key);

    Boolean sIsMember(String key, Object value);

    Long sSize(String key);

    Long sRemove(String key, Object... values);

    void zAdd(String key, Object value, double score);

    Set<Object> zRange(String key, long start, long end);

    Set<Object> zReverseRange(String key, long start, long end);

    Long zSize(String key);

    Long zRemove(String key, Object... values);

    void deleteByPattern(String pattern);

    Set<String> scanKeys(String pattern);

    Long increment(String key);

    Long increment(String key, long delta);

    Long decrement(String key);

    Long decrement(String key, long delta);
}
