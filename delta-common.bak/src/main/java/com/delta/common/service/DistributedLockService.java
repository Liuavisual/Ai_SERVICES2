package com.delta.common.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务接口，基于Redis实现分布式互斥
 *
 * @author 刘建国
 */
public interface DistributedLockService {

    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit);

    void unlock(String lockKey);

    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier);

    void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable);

    boolean isLocked(String lockKey);
}
