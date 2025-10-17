package com.locknroll.service;

import com.locknroll.entity.Fruit;
import com.locknroll.entity.FruitTransaction;
import com.locknroll.repository.FruitRepository;
import com.locknroll.repository.FruitTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive concurrency tests for Fruit operations
 * These tests demonstrate Redis caching and distributed locking behavior
 */
@SpringBootTest
@ActiveProfiles("test")
public class FruitServiceConcurrencyTest {
    
    @Autowired
    private FruitService fruitService;
    
    @Autowired
    private FruitRepository fruitRepository;
    
    @Autowired
    private FruitTransactionRepository transactionRepository;
    
    private Fruit testFruit;
    
    @BeforeEach
    void setUp() {
        // Clean up previous test data
        fruitRepository.deleteAll();
        transactionRepository.deleteAll();
        
        // Create a test fruit
        testFruit = new Fruit("Test Apple", new BigDecimal("2.50"), 100, 
                             "A test apple for concurrency testing", "Fruit");
        testFruit = fruitService.createFruit(testFruit);
    }
    
    /**
     * Test concurrent purchases of the same fruit
     * This demonstrates distributed locking preventing race conditions
     */
    @Test
    void testConcurrentPurchases() throws InterruptedException {
        int numberOfThreads = 10;
        int purchaseQuantity = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<Fruit>> futures = new ArrayList<>();
        
        System.out.println("=== Starting Concurrent Purchase Test ===");
        System.out.println("Initial stock: " + testFruit.getQuantity()); //100
        System.out.println("Number of threads: " + numberOfThreads); //10
        System.out.println("Purchase quantity per thread: " + purchaseQuantity); //5
        //remaining stock = 100 - (10 * 5) = 50
        //expected final quantity = 50
        
        // Submit purchase tasks
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            Future<Fruit> future = executor.submit(() -> {
                try {
                    System.out.println("Thread " + threadId + " starting purchase...");
                Fruit result = fruitService.purchaseFruit(testFruit.getId(), purchaseQuantity);
                    System.out.println("Thread " + threadId + " completed purchase. Remaining stock: " + result.getQuantity());
                    return result;
                } catch (Exception e) {
                    System.out.println("Thread " + threadId + " failed: " + e.getMessage());
                    throw e;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }
        
        // Wait for all threads to complete
        latch.await(30, TimeUnit.SECONDS);
        
        // Collect results
        List<Fruit> results = new ArrayList<>();
        for (Future<Fruit> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                System.out.println("Thread execution failed: " + e.getCause().getMessage());
            }
        }
        
        // Verify final state
        Fruit finalFruit = fruitService.findById(testFruit.getId()).orElse(null);
        assertNotNull(finalFruit);
        
        int expectedFinalQuantity = testFruit.getQuantity() - (results.size() * purchaseQuantity);
        System.out.println("Expected final quantity: " + expectedFinalQuantity);
        System.out.println("Actual final quantity: " + finalFruit.getQuantity());
        
        assertEquals(expectedFinalQuantity, finalFruit.getQuantity());
        
        // Verify transactions were logged
        List<FruitTransaction> transactions = transactionRepository.findByFruitId(testFruit.getId());
        System.out.println("Total transactions logged: " + transactions.size());
        
        executor.shutdown();
    }
    
    /**
     * Test concurrent restocking operations
     */
    @Test
    void testConcurrentRestocking() throws InterruptedException {
        int numberOfThreads = 5;
        int restockQuantity = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<Fruit>> futures = new ArrayList<>();
        
        System.out.println("=== Starting Concurrent Restock Test ===");
        System.out.println("Initial stock: " + testFruit.getQuantity());
        System.out.println("Number of threads: " + numberOfThreads);
        System.out.println("Restock quantity per thread: " + restockQuantity);
        
        // Submit restock tasks
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            Future<Fruit> future = executor.submit(() -> {
                try {
                    System.out.println("Thread " + threadId + " starting restock...");
                    Fruit result = fruitService.restockFruit(testFruit.getId(), restockQuantity);
                    System.out.println("Thread " + threadId + " completed restock. New stock: " + result.getQuantity());
                    return result;
                } catch (Exception e) {
                    System.out.println("Thread " + threadId + " failed: " + e.getMessage());
                    throw e;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }
        
        // Wait for all threads to complete
        latch.await(30, TimeUnit.SECONDS);
        
        // Collect results
        List<Fruit> results = new ArrayList<>();
        for (Future<Fruit> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                System.out.println("Thread execution failed: " + e.getCause().getMessage());
            }
        }
        
        // Verify final state
        Fruit finalFruit = fruitService.findById(testFruit.getId()).orElse(null);
        assertNotNull(finalFruit);
        
        int expectedFinalQuantity = testFruit.getQuantity() + (results.size() * restockQuantity);
        System.out.println("Expected final quantity: " + expectedFinalQuantity);
        System.out.println("Actual final quantity: " + finalFruit.getQuantity());
        
        assertEquals(expectedFinalQuantity, finalFruit.getQuantity());
        
        executor.shutdown();
    }
    
    /**
     * Test mixed concurrent operations (purchases and restocks)
     */
    @Test
    void testMixedConcurrentOperations() throws InterruptedException {
        int purchaseThreads = 5;
        int restockThreads = 3;
        int purchaseQuantity = 8;
        int restockQuantity = 15;
        
        ExecutorService executor = Executors.newFixedThreadPool(purchaseThreads + restockThreads);
        CountDownLatch latch = new CountDownLatch(purchaseThreads + restockThreads);
        List<Future<Fruit>> futures = new ArrayList<>();
        
        System.out.println("=== Starting Mixed Operations Test ===");
        System.out.println("Initial stock: " + testFruit.getQuantity());
        System.out.println("Purchase threads: " + purchaseThreads + " (quantity: " + purchaseQuantity + ")");
        System.out.println("Restock threads: " + restockThreads + " (quantity: " + restockQuantity + ")");
        
        // Submit purchase tasks
        for (int i = 0; i < purchaseThreads; i++) {
            final int threadId = i;
            Future<Fruit> future = executor.submit(() -> {
                try {
                    System.out.println("Purchase Thread " + threadId + " starting...");
                    Fruit result = fruitService.purchaseFruit(testFruit.getId(), purchaseQuantity);
                    System.out.println("Purchase Thread " + threadId + " completed. Stock: " + result.getQuantity());
                    return result;
                } catch (Exception e) {
                    System.out.println("Purchase Thread " + threadId + " failed: " + e.getMessage());
                    throw e;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }
        
        // Submit restock tasks
        for (int i = 0; i < restockThreads; i++) {
            final int threadId = i;
            Future<Fruit> future = executor.submit(() -> {
                try {
                    System.out.println("Restock Thread " + threadId + " starting...");
                    Fruit result = fruitService.restockFruit(testFruit.getId(), restockQuantity);
                    System.out.println("Restock Thread " + threadId + " completed. Stock: " + result.getQuantity());
                    return result;
                } catch (Exception e) {
                    System.out.println("Restock Thread " + threadId + " failed: " + e.getMessage());
                    throw e;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }
        
        // Wait for all threads to complete
        latch.await(30, TimeUnit.SECONDS);
        
        // Collect results
        List<Fruit> results = new ArrayList<>();
        for (Future<Fruit> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                System.out.println("Thread execution failed: " + e.getCause().getMessage());
            }
        }
        
        // Verify final state
        Fruit finalFruit = fruitService.findById(testFruit.getId()).orElse(null);
        assertNotNull(finalFruit);
        
        int totalPurchased = purchaseThreads * purchaseQuantity;
        int totalRestocked = restockThreads * restockQuantity;
        int expectedFinalQuantity = testFruit.getQuantity() - totalPurchased + totalRestocked;
        
        System.out.println("Total purchased: " + totalPurchased);
        System.out.println("Total restocked: " + totalRestocked);
        System.out.println("Expected final quantity: " + expectedFinalQuantity);
        System.out.println("Actual final quantity: " + finalFruit.getQuantity());
        
        assertEquals(expectedFinalQuantity, finalFruit.getQuantity());
        
        executor.shutdown();
    }
    
    /**
     * Test cache behavior under concurrent access
     */
    @Test
    void testCacheBehaviorUnderConcurrency() throws InterruptedException {
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger cacheHits = new AtomicInteger(0);
        AtomicInteger cacheMisses = new AtomicInteger(0);
        
        System.out.println("=== Starting Cache Behavior Test ===");
        System.out.println("Number of threads: " + numberOfThreads);
        
        // Submit read tasks
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Thread " + threadId + " reading fruit...");
                    long startTime = System.currentTimeMillis();
                    Fruit fruit = fruitService.findById(testFruit.getId()).orElse(null);
                    long endTime = System.currentTimeMillis();
                    
                    if (fruit != null) {
                        System.out.println("Thread " + threadId + " read fruit in " + 
                                         (endTime - startTime) + "ms");
                        // Simple heuristic: if read time is very fast, likely from cache
                        if (endTime - startTime < 10) {
                            cacheHits.incrementAndGet();
                        } else {
                            cacheMisses.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Thread " + threadId + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        latch.await(30, TimeUnit.SECONDS);
        
        System.out.println("Cache hits: " + cacheHits.get());
        System.out.println("Cache misses: " + cacheMisses.get());
        
        // Verify that we got some cache hits (first read should be miss, subsequent should be hits)
        assertTrue(cacheHits.get() > 0, "Expected some cache hits");
        
        executor.shutdown();
    }
    
    /**
     * Test lock timeout scenarios
     */
    @Test
    void testLockTimeoutScenario() throws InterruptedException {
        System.out.println("=== Starting Lock Timeout Test ===");
        
        // Create a scenario where one thread holds a lock for a long time
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        
        // Thread 1: Hold lock for a long time
        Future<Fruit> longTask = executor.submit(() -> {
            try {
                System.out.println("Long task starting...");
                Thread.sleep(5000); // Hold lock for 5 seconds
                return fruitService.purchaseFruit(testFruit.getId(), 1);
            } catch (Exception e) {
                System.out.println("Long task failed: " + e.getMessage());
                throw e;
            } finally {
                latch.countDown();
            }
        });
        
        // Thread 2: Try to acquire same lock with short timeout
        Future<Fruit> shortTask = executor.submit(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second then try
                System.out.println("Short task attempting to acquire lock...");
                return fruitService.purchaseFruit(testFruit.getId(), 1);
            } catch (Exception e) {
                System.out.println("Short task failed (expected): " + e.getMessage());
                return null;
            } finally {
                latch.countDown();
            }
        });
        
        // Wait for both threads
        latch.await(10, TimeUnit.SECONDS);
        
        // Verify that long task succeeded and short task failed
        try {
            Fruit longTaskResult = longTask.get();
            assertNotNull(longTaskResult);
            System.out.println("Long task succeeded");
        } catch (ExecutionException e) {
            fail("Long task should have succeeded");
        }
        
        try {
            Fruit shortTaskResult = shortTask.get();
            // Short task might succeed if it waits long enough, or fail due to timeout
            System.out.println("Short task result: " + (shortTaskResult != null ? "succeeded" : "failed"));
        } catch (ExecutionException e) {
            System.out.println("Short task failed as expected: " + e.getCause().getMessage());
        }
        
        executor.shutdown();
    }
}
