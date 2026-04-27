package com.delta.common.service.impl;

import com.delta.common.exception.BusinessException;
import com.delta.common.service.DistributedLockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务实现，基于Redisson
 *
 * @author delta
 */
@Service
public class DistributedLockServiceImpl implements DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockServiceImpl.class);
    
    private static final String LOCK_PREFIX = "delta:lock:";

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        String key = LOCK_PREFIX + lockKey;
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁失败: {}", key, e);
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    @Override
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        String key = LOCK_PREFIX + lockKey;
        RLock lock = redissonClient.getLock(key);
        try {
            if (lock.tryLock(waitTime, leaseTime, unit)) {
                try {
                    return supplier.get();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new BusinessException("获取分布式锁超时: " + key);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("获取分布式锁被中断: " + key);
        }
    }

    @Override
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) {
        String key = LOCK_PREFIX + lockKey;
        RLock lock = redissonClient.getLock(key);
        try {
            if (lock.tryLock(waitTime, leaseTime, unit)) {
                try {
                    runnable.run();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new BusinessException("获取分布式锁超时: " + key);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("获取分布式锁被中断: " + key);
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        RLock lock = redissonClient.getLock(key);
        return lock.isLocked();
    }
}
