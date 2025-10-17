package com.locknroll.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Service for distributed locking using Redisson
 */
@Service
public class DistributedLockService {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedLockService.class);
    
    @Autowired
    private RedissonClient redissonClient;
    
    /**
     * Execute a task with distributed lock
     * 
     * @param lockKey The key for the lock
     * @param waitTime Maximum time to wait for the lock
     * @param leaseTime Time to hold the lock
     * @param timeUnit Time unit for wait and lease times
     * @param task The task to execute
     * @return Result of the task execution
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, 
                                TimeUnit timeUnit, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        long startTime = System.currentTimeMillis();
        boolean lockAcquired = false;
        
        try {
            logger.debug("Thread {} attempting to acquire lock: {}", 
                        Thread.currentThread().getName(), lockKey);
            
            lockAcquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            
            if (lockAcquired) {
                long lockDuration = System.currentTimeMillis() - startTime;
                logger.info("Thread {} acquired lock: {} in {}ms", 
                           Thread.currentThread().getName(), lockKey, lockDuration);
                
                return task.get();
            } else {
                logger.warn("Thread {} failed to acquire lock: {} within {}ms", 
                           Thread.currentThread().getName(), lockKey, 
                           timeUnit.toMillis(waitTime));
                throw new RuntimeException("Failed to acquire lock: " + lockKey);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread {} was interrupted while waiting for lock: {}", 
                        Thread.currentThread().getName(), lockKey);
            throw new RuntimeException("Interrupted while waiting for lock: " + lockKey, e);
        } finally {
            if (lockAcquired && lock.isHeldByCurrentThread()) {
                long totalDuration = System.currentTimeMillis() - startTime;
                logger.info("Thread {} releasing lock: {} after {}ms", 
                           Thread.currentThread().getName(), lockKey, totalDuration);
                lock.unlock();
            }
        }
    }
    
    /**
     * Execute a task with distributed lock (default timeout: 10 seconds wait, 30 seconds lease)
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        return executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, task);
    }
    
    /**
     * Execute a task with distributed lock and return lock acquisition info
     */
    public <T> LockResult<T> executeWithLockInfo(String lockKey, long waitTime, long leaseTime, 
                                                TimeUnit timeUnit, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        long startTime = System.currentTimeMillis();
        boolean lockAcquired = false;
        
        try {
            logger.debug("Thread {} attempting to acquire lock: {}", 
                        Thread.currentThread().getName(), lockKey);
            
            lockAcquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            long lockDuration = System.currentTimeMillis() - startTime;
            
            if (lockAcquired) {
                logger.info("Thread {} acquired lock: {} in {}ms", 
                           Thread.currentThread().getName(), lockKey, lockDuration);
                
                T result = task.get();
                return new LockResult<>(result, true, lockDuration, null);
            } else {
                logger.warn("Thread {} failed to acquire lock: {} within {}ms", 
                           Thread.currentThread().getName(), lockKey, 
                           timeUnit.toMillis(waitTime));
                return new LockResult<>(null, false, lockDuration, 
                                      "Failed to acquire lock within timeout");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long lockDuration = System.currentTimeMillis() - startTime;
            logger.error("Thread {} was interrupted while waiting for lock: {}", 
                        Thread.currentThread().getName(), lockKey);
            return new LockResult<>(null, false, lockDuration, 
                                  "Interrupted while waiting for lock: " + e.getMessage());
        } catch (Exception e) {
            long lockDuration = System.currentTimeMillis() - startTime;
            logger.error("Thread {} error while executing with lock: {}", 
                        Thread.currentThread().getName(), lockKey, e);
            return new LockResult<>(null, lockAcquired, lockDuration, e.getMessage());
        } finally {
            if (lockAcquired && lock.isHeldByCurrentThread()) {
                long totalDuration = System.currentTimeMillis() - startTime;
                logger.info("Thread {} releasing lock: {} after {}ms", 
                           Thread.currentThread().getName(), lockKey, totalDuration);
                lock.unlock();
            }
        }
    }
    
    /**
     * Check if a lock is currently held
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
    
    /**
     * Result wrapper for lock operations
     */
    public static class LockResult<T> {
        private final T result;
        private final boolean lockAcquired;
        private final long lockDurationMs;
        private final String errorMessage;
        
        public LockResult(T result, boolean lockAcquired, long lockDurationMs, String errorMessage) {
            this.result = result;
            this.lockAcquired = lockAcquired;
            this.lockDurationMs = lockDurationMs;
            this.errorMessage = errorMessage;
        }
        
        public T getResult() {
            return result;
        }
        
        public boolean isLockAcquired() {
            return lockAcquired;
        }
        
        public long getLockDurationMs() {
            return lockDurationMs;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public boolean isSuccess() {
            return lockAcquired && errorMessage == null;
        }
    }
}
