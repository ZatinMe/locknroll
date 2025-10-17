package com.locknroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot Application for Redis Learning Project
 * 
 * This application demonstrates:
 * - Redis caching with Spring Boot
 * - Distributed locking using Redisson
 * - Multi-threaded resource access scenarios
 * - Integration with PostgreSQL and MongoDB
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class RedisLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisLearningApplication.class, args);
    }
}

