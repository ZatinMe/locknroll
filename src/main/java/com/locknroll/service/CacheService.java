package com.locknroll.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Service for Redis caching operations
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "locknroll:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    /**
     * Cache a value with default TTL
     */
    public void cache(String key, Object value) {
        cache(key, value, DEFAULT_TTL);
    }

    /**
     * Cache a value with custom TTL
     */
    public void cache(String key, Object value, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            String cacheKey = CACHE_PREFIX + key;
            redisTemplate.opsForValue().set(cacheKey, jsonValue, ttl);
            logger.debug("Cached value for key: {}", cacheKey);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize value for caching: {}", e.getMessage());
        }
    }

    /**
     * Retrieve a cached value
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String cacheKey = CACHE_PREFIX + key;
            String jsonValue = redisTemplate.opsForValue().get(cacheKey);
            if (jsonValue != null) {
                T value = objectMapper.readValue(jsonValue, type);
                logger.debug("Retrieved cached value for key: {}", cacheKey);
                return Optional.of(value);
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize cached value: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Check if a key exists in cache
     */
    public boolean exists(String key) {
        String cacheKey = CACHE_PREFIX + key;
        Boolean exists = redisTemplate.hasKey(cacheKey);
        return exists != null && exists;
    }

    /**
     * Delete a cached value
     */
    public void evict(String key) {
        String cacheKey = CACHE_PREFIX + key;
        redisTemplate.delete(cacheKey);
        logger.debug("Evicted cache for key: {}", cacheKey);
    }

    /**
     * Delete multiple cached values by pattern
     */
    public void evictByPattern(String pattern) {
        String cachePattern = CACHE_PREFIX + pattern;
        redisTemplate.delete(redisTemplate.keys(cachePattern));
        logger.debug("Evicted cache for pattern: {}", cachePattern);
    }

    /**
     * Cache user session data
     */
    public void cacheUserSession(String username, Object userData) {
        cache("user:session:" + username, userData, Duration.ofHours(24));
    }

    /**
     * Get cached user session data
     */
    public <T> Optional<T> getCachedUserSession(String username, Class<T> type) {
        return get("user:session:" + username, type);
    }

    /**
     * Cache workflow data
     */
    public void cacheWorkflow(Long workflowId, Object workflowData) {
        cache("workflow:" + workflowId, workflowData, Duration.ofHours(1));
    }

    /**
     * Get cached workflow data
     */
    public <T> Optional<T> getCachedWorkflow(Long workflowId, Class<T> type) {
        return get("workflow:" + workflowId, type);
    }

    /**
     * Cache user tasks
     */
    public void cacheUserTasks(Long userId, Object tasksData) {
        cache("user:tasks:" + userId, tasksData, Duration.ofMinutes(10));
    }

    /**
     * Get cached user tasks
     */
    public <T> Optional<T> getCachedUserTasks(Long userId, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
        try {
            String cacheKey = CACHE_PREFIX + "user:tasks:" + userId;
            String jsonValue = redisTemplate.opsForValue().get(cacheKey);
            if (jsonValue != null) {
                logger.debug("Retrieved cached user tasks for user: {}", userId);
                return Optional.of(objectMapper.readValue(jsonValue, typeReference));
            }
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing cached user tasks for user {}: {}", userId, e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Cache dashboard data
     */
    public void cacheDashboardData(Long userId, Object dashboardData) {
        cache("dashboard:" + userId, dashboardData, Duration.ofMinutes(5));
    }

    /**
     * Get cached dashboard data
     */
    public <T> Optional<T> getCachedDashboardData(Long userId, Class<T> type) {
        return get("dashboard:" + userId, type);
    }

    /**
     * Clear all cache
     */
    public void clearAllCache() {
        redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX + "*"));
        logger.info("Cleared all cache");
    }
}
