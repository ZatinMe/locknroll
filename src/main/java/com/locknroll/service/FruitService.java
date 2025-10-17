package com.locknroll.service;

import com.locknroll.entity.Fruit;
import com.locknroll.entity.FruitTransaction;
import com.locknroll.repository.FruitRepository;
import com.locknroll.repository.FruitTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Fruit operations with Redis caching and distributed locking
 */
@Service
@Transactional
public class FruitService {
    
    private static final Logger logger = LoggerFactory.getLogger(FruitService.class);
    
    @Autowired
    private FruitRepository fruitRepository;
    
    @Autowired
    private FruitTransactionRepository transactionRepository;
    
    @Autowired
    private DistributedLockService lockService;
    
    /**
     * Get fruit by ID with caching
     */
    @Cacheable(value = "fruits", key = "#id")
    public Optional<Fruit> findById(Long id) {
        logger.debug("Fetching fruit from database: {}", id);
        return fruitRepository.findById(id);
    }
    
    /**
     * Get fruit by name with caching
     */
    // @Cacheable checks the cache first before executing the method
    // If the value exists in cache, returns cached value without executing method
    // If not found in cache, executes method and caches the result
    // Unlike @CachePut which always executes method and updates cache
    @Cacheable(value = "fruits", key = "'name:' + #name")
    public Optional<Fruit> findByName(String name) {
        logger.debug("Fetching fruit by name from database: {}", name);
        return fruitRepository.findByName(name);
    }
    
    /**
     * Get all fruits
     */
    public List<Fruit> findAll() {
        logger.debug("Fetching all fruits from database");
        return fruitRepository.findAll();
    }
    
    /**
     * Create a new fruit
     */
    // @CachePut updates the cache with the new fruit after creation
    // The key uses #result.id since the ID is only available after saving to DB
    // This ensures the newly created fruit is cached and immediately available for subsequent reads
    @CachePut(value = "fruits", key = "#result.id")
    public Fruit createFruit(Fruit fruit) {
        logger.info("Creating new fruit: {}", fruit.getName());
        
        // Check if fruit already exists
        if (fruitRepository.existsByName(fruit.getName())) {
            throw new RuntimeException("Fruit with name '" + fruit.getName() + "' already exists");
        }
        
        Fruit savedFruit = fruitRepository.save(fruit);
        
        // Log transaction
        logTransaction(savedFruit.getId(), savedFruit.getName(), "CREATE", 
                      null, savedFruit.getQuantity(), null, savedFruit.getPrice());
        
        logger.info("Created fruit: {}", savedFruit);
        return savedFruit;
    }
    
    /**
     * Update fruit with distributed locking
     * 
     * Note on caching:
     * - The updated fruit is not automatically cached since no @CachePut is used
     * - Instead, we explicitly evict the old cache entries using evictFruitCache()
     * - This removes both ID-based and name-based cache entries
     * - On next read, the updated fruit will be fetched from DB and cached again
     * - This approach prevents stale cache data while letting @Cacheable handle caching
     */
    public Fruit updateFruit(Long id, Fruit updatedFruit) {
        String lockKey = "fruit:update:" + id; //fruit:update:2
        
        return lockService.executeWithLock(lockKey, () -> updateFruitInternal(id, updatedFruit));
    }
    
    /**
     * Internal method to update fruit without locking
     * This method contains the actual update logic and is called within the distributed lock
     */
    private Fruit updateFruitInternal(Long id, Fruit updatedFruit) {
        logger.info("Thread {} updating fruit: {}", Thread.currentThread().getName(), id);
        
        Fruit existingFruit = fruitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fruit not found with id: " + id));
        
        // Store old values for transaction log
        Integer oldQuantity = existingFruit.getQuantity();
        BigDecimal oldPrice = existingFruit.getPrice();
        
        // Update fields
        existingFruit.setName(updatedFruit.getName());
        existingFruit.setPrice(updatedFruit.getPrice());
        existingFruit.setQuantity(updatedFruit.getQuantity());
        existingFruit.setDescription(updatedFruit.getDescription());
        existingFruit.setCategory(updatedFruit.getCategory());
        
        Fruit savedFruit = fruitRepository.save(existingFruit);
        
        // Evict cache
        evictFruitCache(id, savedFruit.getName());
        
        // Log transaction
        logTransaction(id, savedFruit.getName(), "UPDATE", 
                      oldQuantity, savedFruit.getQuantity(), 
                      oldPrice, savedFruit.getPrice());
        
        logger.info("Thread {} updated fruit: {}", Thread.currentThread().getName(), savedFruit);
        return savedFruit;
    }
    
    /**
     * Purchase fruit (decrease quantity) with distributed locking
     */
    public Fruit purchaseFruit(Long id, Integer quantity) {
        String lockKey = "fruit:purchase:" + id;
        
        return lockService.executeWithLock(lockKey, () -> {
            logger.info("Thread {} purchasing {} units of fruit: {}", 
                       Thread.currentThread().getName(), quantity, id);
            
            Fruit fruit = fruitRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fruit not found with id: " + id));
            
            if (fruit.getQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock. Available: " + fruit.getQuantity() + 
                                         ", Requested: " + quantity);
            }
            
            Integer oldQuantity = fruit.getQuantity();
            fruit.setQuantity(fruit.getQuantity() - quantity);
            
            Fruit savedFruit = fruitRepository.save(fruit);
            
            // Evict cache
            evictFruitCache(id, savedFruit.getName());
            
            // Log transaction
            logTransaction(id, savedFruit.getName(), "PURCHASE", 
                          oldQuantity, savedFruit.getQuantity(), 
                          savedFruit.getPrice(), savedFruit.getPrice());
            
            logger.info("Thread {} purchased {} units of fruit: {}. Remaining: {}", 
                       Thread.currentThread().getName(), quantity, savedFruit.getName(), 
                       savedFruit.getQuantity());
            
            return savedFruit;
        });
    }
    /**
     * Helper method to get consistent lock key for fruit operations
     * 
     * Important: Since updateFruit can also modify quantity, we need to:
     * 1. Use the same lock key for all quantity-affecting operations to ensure
     *    proper synchronization and prevent race conditions
     * 2. Check if quantity is being modified in updateFruit to determine if we 
     *    need the quantity lock
     * 3. Use optimistic locking with @Version to prevent stale data updates
     * 
     * Lock key pattern:
     * - All quantity operations: "fruit:quantity:{id}"
     */
    private String getFruitQuantityLockKey(Long id) {
        return "fruit:quantity:" + id;
    }
    
    /**
     * Helper method to determine if an update operation affects quantity
     * Used to decide if quantity lock is needed
     */
    private boolean isQuantityUpdate(Fruit existingFruit, Fruit updatedFruit) {
        return !existingFruit.getQuantity().equals(updatedFruit.getQuantity());
    }
    
    /**
     * Note on concurrency control strategy:
     * 1. Distributed Lock Level:
     *    - All quantity modifications (purchase/restock/update) must acquire the same
     *      quantity lock to ensure consistency across services/nodes
     *    - The updateFruit method should check if quantity is being modified and
     *      acquire the quantity lock if needed
     * 
     * 2. Database Level:
     *    - Using @Version for optimistic locking in Fruit entity
     *    - This prevents stale reads by checking version number during updates
     *    - If version mismatch occurs, throws OptimisticLockingFailureException
     *    - Example:
     *      @Version
     *      private Long version; // In Fruit entity
     * 
     * This dual-level approach ensures:
     * - Distributed synchronization via Redis locks
     * - Data consistency via JPA optimistic locking
     * - Protection against both distributed race conditions and stale data updates
     */
    /**
     * Restock fruit (increase quantity) with distributed locking
     */
    public Fruit restockFruit(Long id, Integer quantity) {
        String lockKey = "fruit:restock:" + id;
        
        return lockService.executeWithLock(lockKey, () -> {
            logger.info("Thread {} restocking {} units of fruit: {}", 
                       Thread.currentThread().getName(), quantity, id);
            
            Fruit fruit = fruitRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fruit not found with id: " + id));
            
            Integer oldQuantity = fruit.getQuantity();
            fruit.setQuantity(fruit.getQuantity() + quantity);
            
            Fruit savedFruit = fruitRepository.save(fruit);
            
            // Evict cache
            evictFruitCache(id, savedFruit.getName());
            
            // Log transaction
            logTransaction(id, savedFruit.getName(), "RESTOCK", 
                          oldQuantity, savedFruit.getQuantity(), 
                          savedFruit.getPrice(), savedFruit.getPrice());
            
            logger.info("Thread {} restocked {} units of fruit: {}. New total: {}", 
                       Thread.currentThread().getName(), quantity, savedFruit.getName(), 
                       savedFruit.getQuantity());
            
            return savedFruit;
        });
    }
    
    /**
     * Delete fruit
     */
    @CacheEvict(value = "fruits", key = "#id")
    public void deleteFruit(Long id) {
        logger.info("Deleting fruit: {}", id);
        
        Fruit fruit = fruitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fruit not found with id: " + id));
        
        fruitRepository.deleteById(id);
        
        // Log transaction
        logTransaction(id, fruit.getName(), "DELETE", 
                      fruit.getQuantity(), 0, fruit.getPrice(), null);
        
        logger.info("Deleted fruit: {}", fruit.getName());
    }
    
    /**
     * Get fruits by category
     */
    public List<Fruit> findByCategory(String category) {
        logger.debug("Fetching fruits by category: {}", category);
        return fruitRepository.findByCategory(category);
    }
    
    /**
     * Get low stock fruits
     */
    public List<Fruit> getLowStockFruits(Integer threshold) {
        logger.debug("Fetching low stock fruits with threshold: {}", threshold);
        return fruitRepository.findLowStockFruits(threshold);
    }
    
    /**
     * Evict fruit cache entries
     */
    @CacheEvict(value = "fruits", key = "#id")
    public void evictFruitCache(Long id) {
        logger.debug("Evicting cache for fruit: {}", id);
    }
    
    @CacheEvict(value = "fruits", key = "'name:' + #name")
    public void evictFruitCacheByName(String name) {
        logger.debug("Evicting cache for fruit name: {}", name);
    }
    
    private void evictFruitCache(Long id, String name) {
        evictFruitCache(id);
        evictFruitCacheByName(name);
    }
    
    /**
     * Log transaction to MongoDB
     */
    private void logTransaction(Long fruitId, String fruitName, String operationType,
                               Integer oldQuantity, Integer newQuantity,
                               BigDecimal oldPrice, BigDecimal newPrice) {
        try {
            FruitTransaction transaction = new FruitTransaction(fruitId, fruitName, operationType);
            transaction.setOldQuantity(oldQuantity);
            transaction.setNewQuantity(newQuantity);
            transaction.setOldPrice(oldPrice);
            transaction.setNewPrice(newPrice);
            transaction.setQuantityChange(newQuantity != null && oldQuantity != null ? 
                                        newQuantity - oldQuantity : null);
            transaction.setPriceChange(newPrice != null && oldPrice != null ? 
                                     newPrice.subtract(oldPrice) : null);
            transaction.setUserId("system");
            transaction.setSessionId(UUID.randomUUID().toString());
            transaction.setThreadName(Thread.currentThread().getName());
            transaction.setTimestamp(LocalDateTime.now());
            
            transactionRepository.save(transaction);
            logger.debug("Logged transaction: {}", transaction);
        } catch (Exception e) {
            logger.error("Failed to log transaction for fruit: {}", fruitId, e);
        }
    }
}
