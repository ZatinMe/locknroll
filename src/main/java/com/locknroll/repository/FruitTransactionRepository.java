package com.locknroll.repository;

import com.locknroll.entity.FruitTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for FruitTransaction entity operations
 */
@Repository
public interface FruitTransactionRepository extends MongoRepository<FruitTransaction, String> {
    
    /**
     * Find transactions by fruit ID
     */
    List<FruitTransaction> findByFruitId(Long fruitId);
    
    /**
     * Find transactions by operation type
     */
    List<FruitTransaction> findByOperationType(String operationType);
    
    /**
     * Find transactions by user ID
     */
    List<FruitTransaction> findByUserId(String userId);
    
    /**
     * Find transactions by session ID
     */
    List<FruitTransaction> findBySessionId(String sessionId);
    
    /**
     * Find transactions by thread name
     */
    List<FruitTransaction> findByThreadName(String threadName);
    
    /**
     * Find transactions within time range
     */
    List<FruitTransaction> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find transactions where lock was acquired
     */
    List<FruitTransaction> findByLockAcquiredTrue();
    
    /**
     * Find transactions where cache was hit
     */
    List<FruitTransaction> findByCacheHitTrue();
    
    /**
     * Find transactions with lock duration greater than threshold
     */
    @Query("{ 'lockDurationMs': { $gt: ?0 } }")
    List<FruitTransaction> findByLockDurationGreaterThan(Long thresholdMs);
    
    /**
     * Find recent transactions for a specific fruit
     */
    @Query("{ 'fruitId': ?0, 'timestamp': { $gte: ?1 } }")
    List<FruitTransaction> findRecentTransactionsForFruit(Long fruitId, LocalDateTime since);
    
    /**
     * Count transactions by operation type
     */
    long countByOperationType(String operationType);
    
    /**
     * Find transactions by multiple criteria
     */
    @Query("{ 'fruitId': ?0, 'operationType': ?1, 'timestamp': { $gte: ?2 } }")
    List<FruitTransaction> findByFruitIdAndOperationTypeAndTimestampAfter(
            Long fruitId, String operationType, LocalDateTime timestamp);
}
