package com.locknroll.integration;

import com.locknroll.entity.Fruit;
import com.locknroll.entity.FruitTransaction;
import com.locknroll.repository.FruitRepository;
import com.locknroll.repository.FruitTransactionRepository;
import com.locknroll.service.DistributedLockService;
import com.locknroll.service.FruitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Redis functionality
 * Tests caching, distributed locking, and transaction logging
 */
@SpringBootTest
@ActiveProfiles("test")
public class RedisIntegrationTest {
    
    @Autowired
    private FruitService fruitService;
    
    @Autowired
    private FruitRepository fruitRepository;
    
    @Autowired
    private FruitTransactionRepository transactionRepository;
    
    @Autowired
    private DistributedLockService lockService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @BeforeEach
    void setUp() {
        // Clean up test data
        fruitRepository.deleteAll();
        transactionRepository.deleteAll();
        
        // Clear Redis cache
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
    
    /**
     * Test Redis caching functionality
     */
    @Test
    void testRedisCaching() {
        System.out.println("=== Testing Redis Caching ===");
        
        // Create a fruit
        Fruit fruit = new Fruit("Cache Test Apple", new BigDecimal("3.00"), 50, 
                               "Apple for cache testing", "Fruit");
        fruit = fruitService.createFruit(fruit);
        
        // First read - should hit database
        long startTime = System.currentTimeMillis();
        Fruit firstRead = fruitService.findById(fruit.getId()).orElse(null);
        long firstReadTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(firstRead);
        System.out.println("First read time: " + firstReadTime + "ms");
        
        // Second read - should hit cache
        startTime = System.currentTimeMillis();
        Fruit secondRead = fruitService.findById(fruit.getId()).orElse(null);
        long secondReadTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(secondRead);
        System.out.println("Second read time: " + secondReadTime + "ms");
        
        // Cache read should be faster
        assertTrue(secondReadTime < firstReadTime, 
                  "Cache read should be faster than database read");
        
        // Test cache eviction
        fruitService.evictFruitCache(fruit.getId());
        
        // Third read - should hit database again
        startTime = System.currentTimeMillis();
        Fruit thirdRead = fruitService.findById(fruit.getId()).orElse(null);
        long thirdReadTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(thirdRead);
        System.out.println("Third read time (after eviction): " + thirdReadTime + "ms");
    }
    
    /**
     * Test distributed locking with detailed logging
     */
    @Test
    void testDistributedLocking() throws InterruptedException {
        System.out.println("=== Testing Distributed Locking ===");
        
        // Create a fruit
        Fruit fruit = new Fruit("Lock Test Apple", new BigDecimal("2.00"), 100, 
                               "Apple for lock testing", "Fruit");
        final Fruit createdFruit = fruitService.createFruit(fruit);
        
        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        System.out.println("Starting " + numberOfThreads + " concurrent purchase threads...");
        
        // Submit concurrent purchase tasks
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Thread " + threadId + " starting purchase...");
                    long startTime = System.currentTimeMillis();
                    
                    final Long fruitId = createdFruit.getId();
                    Fruit result = fruitService.purchaseFruit(fruitId, 10);
                    
                    long endTime = System.currentTimeMillis();
                    System.out.println("Thread " + threadId + " completed purchase in " + 
                                     (endTime - startTime) + "ms. Remaining stock: " + result.getQuantity());
                    
                } catch (Exception e) {
                    System.out.println("Thread " + threadId + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "All threads should complete within timeout");
        
        // Verify final state
        Fruit finalFruit = fruitService.findById(fruit.getId()).orElse(null);
        assertNotNull(finalFruit);
        
        int expectedQuantity = 100 - (numberOfThreads * 10);
        assertEquals(expectedQuantity, finalFruit.getQuantity(), 
                   "Final quantity should be correct after concurrent operations");
        
        System.out.println("Final stock: " + finalFruit.getQuantity());
        
        executor.shutdown();
    }
    
    /**
     * Test transaction logging to MongoDB
     */
    @Test
    void testTransactionLogging() {
        System.out.println("=== Testing Transaction Logging ===");
        
        // Create a fruit
        Fruit fruit = new Fruit("Transaction Test Apple", new BigDecimal("1.50"), 75, 
                               "Apple for transaction testing", "Fruit");
        fruit = fruitService.createFruit(fruit);
        
        // Perform various operations
        fruitService.purchaseFruit(fruit.getId(), 10);
        fruitService.restockFruit(fruit.getId(), 5);
        fruitService.purchaseFruit(fruit.getId(), 3);
        
        // Check transaction logs
        List<FruitTransaction> transactions = transactionRepository.findByFruitId(fruit.getId());
        
        System.out.println("Total transactions logged: " + transactions.size());
        
        // Should have at least 4 transactions (CREATE, PURCHASE, RESTOCK, PURCHASE)
        assertTrue(transactions.size() >= 4, "Should have logged all transactions");
        
        // Verify transaction details
        for (FruitTransaction transaction : transactions) {
            System.out.println("Transaction: " + transaction.getOperationType() + 
                             " - Thread: " + transaction.getThreadName() + 
                             " - Timestamp: " + transaction.getTimestamp());
            
            assertNotNull(transaction.getOperationType());
            assertNotNull(transaction.getThreadName());
            assertNotNull(transaction.getTimestamp());
        }
        
        // Check specific operation types
        long createCount = transactions.stream()
                .mapToLong(t -> "CREATE".equals(t.getOperationType()) ? 1 : 0)
                .sum();
        long purchaseCount = transactions.stream()
                .mapToLong(t -> "PURCHASE".equals(t.getOperationType()) ? 1 : 0)
                .sum();
        long restockCount = transactions.stream()
                .mapToLong(t -> "RESTOCK".equals(t.getOperationType()) ? 1 : 0)
                .sum();
        
        assertEquals(1, createCount, "Should have 1 CREATE transaction");
        assertEquals(2, purchaseCount, "Should have 2 PURCHASE transactions");
        assertEquals(1, restockCount, "Should have 1 RESTOCK transaction");
    }
    
    /**
     * Test lock service directly
     */
    @Test
    void testLockServiceDirectly() throws InterruptedException {
        System.out.println("=== Testing Lock Service Directly ===");
        
        String lockKey = "test:lock:direct";
        int numberOfThreads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        // Test concurrent lock acquisition
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Thread " + threadId + " attempting to acquire lock...");
                    
                    DistributedLockService.LockResult<String> result = lockService.executeWithLockInfo(
                            lockKey, 5, 10, TimeUnit.SECONDS, () -> {
                                System.out.println("Thread " + threadId + " acquired lock, executing task...");
                                try {
                                    Thread.sleep(2000); // Simulate work
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw new RuntimeException(e);
                                }
                                return "Task completed by thread " + threadId;
                            });
                    
                    if (result.isSuccess()) {
                        System.out.println("Thread " + threadId + " succeeded: " + result.getResult());
                    } else {
                        System.out.println("Thread " + threadId + " failed: " + result.getErrorMessage());
                    }
                    
                } catch (Exception e) {
                    System.out.println("Thread " + threadId + " exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "All threads should complete");
        
        executor.shutdown();
    }
    
    /**
     * Test cache invalidation scenarios
     */
    @Test
    void testCacheInvalidation() {
        System.out.println("=== Testing Cache Invalidation ===");
        
        // Create a fruit
        Fruit fruit = new Fruit("Cache Invalidation Apple", new BigDecimal("4.00"), 25, 
                               "Apple for cache invalidation testing", "Fruit");
        fruit = fruitService.createFruit(fruit);
        
        // Read fruit to populate cache
        Fruit cachedFruit = fruitService.findById(fruit.getId()).orElse(null);
        assertNotNull(cachedFruit);
        System.out.println("Fruit cached: " + cachedFruit.getName());
        
        // Update fruit (should invalidate cache)
        Fruit updatedFruit = new Fruit("Updated Apple", new BigDecimal("4.50"), 30, 
                                      "Updated description", "Fruit");
        fruitService.updateFruit(fruit.getId(), updatedFruit);
        
        // Read again - should get updated data
        Fruit freshFruit = fruitService.findById(fruit.getId()).orElse(null);
        assertNotNull(freshFruit);
        
        assertEquals("Updated Apple", freshFruit.getName());
        assertEquals(new BigDecimal("4.50"), freshFruit.getPrice());
        assertEquals(30, freshFruit.getQuantity());
        
        System.out.println("Cache invalidation successful - got updated data");
    }
}
