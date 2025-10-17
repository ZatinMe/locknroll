# Redis Learning Project - LockN'Roll

A comprehensive Spring Boot application demonstrating Redis caching and distributed locking patterns with PostgreSQL and MongoDB integration.

## 🎯 Project Overview

This project is designed to help you learn Redis concepts through practical implementation:

- **Redis Caching**: Spring Boot cache abstraction with Redis backend
- **Distributed Locking**: Using Redisson for thread-safe operations
- **Multi-threaded Testing**: Comprehensive test scenarios with concurrent access
- **Transaction Logging**: MongoDB integration for audit trails
- **Real-world Use Cases**: Fruit inventory management system

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │    │      Redis      │    │   PostgreSQL    │
│   Application   │◄──►│   (Cache +      │    │   (Fruit Data)  │
│                 │    │    Locks)       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   Distributed   │    │   MongoDB       │
│   Endpoints     │    │   Locking       │    │   (Transactions)│
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 13+
- MongoDB 4.4+
- Redis 6.0+

### 1. Database Setup

#### PostgreSQL Setup
```bash
# Create database
createdb locknroll_db

# Create test database
createdb locknroll_test_db
```

#### MongoDB Setup
```bash
# MongoDB will create databases automatically
# No manual setup required
```

#### Redis Setup
```bash
# Install Redis (macOS)
brew install redis

# Start Redis
redis-server

# Verify Redis is running
redis-cli ping
# Should return: PONG
```

### 2. Application Setup

```bash
# Clone and navigate to project
cd lockNroll

# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Verify Setup

```bash
# Check if all services are running
curl http://localhost:8080/actuator/health

# Get all fruits (should return sample data)
curl http://localhost:8080/api/fruits
```

## 📚 Learning Modules

### Module 1: Redis Caching Basics

**What you'll learn:**
- Spring Boot cache abstraction
- Redis as cache backend
- Cache eviction strategies
- Cache performance benefits

**Key Files:**
- `FruitService.java` - Cache annotations (`@Cacheable`, `@CachePut`, `@CacheEvict`)
- `RedisConfig.java` - Cache configuration
- `FruitServiceConcurrencyTest.java` - Cache behavior tests

**Hands-on Exercise:**
```bash
# 1. Start the application
mvn spring-boot:run

# 2. Make multiple requests to see cache behavior
curl http://localhost:8080/api/fruits/1
curl http://localhost:8080/api/fruits/1  # Should be faster (cached)

# 3. Update the fruit to see cache invalidation
curl -X PUT http://localhost:8080/api/fruits/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Apple","price":3.00,"quantity":90,"description":"Updated description","category":"Fruit"}'

# 4. Check cache invalidation
curl http://localhost:8080/api/fruits/1  # Should fetch fresh data
```

### Module 2: Distributed Locking

**What you'll learn:**
- Race condition prevention
- Distributed lock patterns
- Lock timeout handling
- Thread-safe operations

**Key Files:**
- `DistributedLockService.java` - Lock implementation
- `FruitService.java` - Lock usage in business logic
- `FruitServiceConcurrencyTest.java` - Concurrency tests

**Hands-on Exercise:**
```bash
# Run concurrency tests to see locking in action
mvn test -Dtest=FruitServiceConcurrencyTest#testConcurrentPurchases

# Watch the logs to see lock acquisition and release
```

### Module 3: Multi-threaded Scenarios

**What you'll learn:**
- Concurrent access patterns
- Thread safety verification
- Performance under load
- Debugging concurrent issues

**Test Scenarios:**
1. **Concurrent Purchases**: Multiple threads buying the same fruit
2. **Mixed Operations**: Simultaneous purchases and restocks
3. **Cache Behavior**: Cache hits/misses under concurrency
4. **Lock Timeouts**: Handling lock contention

**Run All Tests:**
```bash
mvn test
```

### Module 4: Transaction Logging

**What you'll learn:**
- Audit trail implementation
- MongoDB integration
- Transaction tracking
- Debugging with logs

**Key Files:**
- `FruitTransaction.java` - Transaction entity
- `FruitTransactionRepository.java` - MongoDB repository
- `FruitService.java` - Transaction logging

**Explore Transactions:**
```bash
# 1. Perform some operations
curl -X POST http://localhost:8080/api/fruits/1/purchase?quantity=5
curl -X POST http://localhost:8080/api/fruits/1/restock?quantity=10

# 2. Check MongoDB for transaction logs
# Connect to MongoDB and query:
# db.fruit_transactions.find().sort({timestamp: -1}).limit(10)
```

## 🧪 Testing Guide

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FruitServiceConcurrencyTest

# Run specific test method
mvn test -Dtest=FruitServiceConcurrencyTest#testConcurrentPurchases

# Run with verbose output
mvn test -Dtest=FruitServiceConcurrencyTest -X
```

### Understanding Test Output

When you run concurrency tests, you'll see output like:
```
=== Starting Concurrent Purchase Test ===
Initial stock: 100
Number of threads: 10
Purchase quantity per thread: 5
Thread 0 starting purchase...
Thread 1 starting purchase...
...
Thread 0 acquired lock: fruit:purchase:1 in 15ms
Thread 0 completed purchase. Remaining stock: 95
...
Expected final quantity: 50
Actual final quantity: 50
Total transactions logged: 10
```

### Key Observations

1. **Lock Acquisition**: Notice the time it takes to acquire locks
2. **Sequential Processing**: Operations are processed one at a time
3. **Data Consistency**: Final quantities are always correct
4. **Transaction Logging**: All operations are logged with thread information

## 🔍 Monitoring and Debugging

### Application Logs

The application provides detailed logging:

```bash
# View application logs
tail -f logs/redis-learning.log

# Filter for specific operations
grep "Thread" logs/redis-learning.log
grep "lock" logs/redis-learning.log
grep "cache" logs/redis-learning.log
```

### Redis Monitoring

```bash
# Connect to Redis CLI
redis-cli

# Monitor Redis commands
MONITOR

# Check cache keys
KEYS fruits:*

# Check lock keys
KEYS *lock*

# Get key information
INFO keyspace
```

### Database Queries

**PostgreSQL:**
```sql
-- Check fruit data
SELECT * FROM fruits ORDER BY id;

-- Check recent updates
SELECT * FROM fruits WHERE updated_at > NOW() - INTERVAL '1 hour';
```

**MongoDB:**
```javascript
// Check transaction logs
db.fruit_transactions.find().sort({timestamp: -1}).limit(10);

// Count operations by type
db.fruit_transactions.aggregate([
  {$group: {_id: "$operationType", count: {$sum: 1}}}
]);

// Find transactions with lock info
db.fruit_transactions.find({lockAcquired: true});
```

## 🎮 Interactive Examples

### Example 1: Cache Performance Test

```bash
# Create a script to test cache performance
cat > test_cache.sh << 'EOF'
#!/bin/bash
echo "Testing cache performance..."

# First request (cache miss)
echo "First request (cache miss):"
time curl -s http://localhost:8080/api/fruits/1 > /dev/null

# Second request (cache hit)
echo "Second request (cache hit):"
time curl -s http://localhost:8080/api/fruits/1 > /dev/null

# Third request (cache hit)
echo "Third request (cache hit):"
time curl -s http://localhost:8080/api/fruits/1 > /dev/null
EOF

chmod +x test_cache.sh
./test_cache.sh
```

### Example 2: Concurrency Stress Test

```bash
# Create a script to simulate concurrent purchases
cat > stress_test.sh << 'EOF'
#!/bin/bash
echo "Starting stress test..."

# Start multiple concurrent purchases
for i in {1..20}; do
  curl -X POST "http://localhost:8080/api/fruits/1/purchase?quantity=1" &
done

# Wait for all requests to complete
wait

echo "Stress test completed. Check the logs for lock behavior."
EOF

chmod +x stress_test.sh
./stress_test.sh
```

## 🚨 Common Issues and Solutions

### Issue 1: Redis Connection Failed
```
Error: Unable to connect to Redis
```
**Solution:**
```bash
# Check if Redis is running
redis-cli ping

# Start Redis if not running
redis-server
```

### Issue 2: Database Connection Issues
```
Error: Connection to PostgreSQL failed
```
**Solution:**
```bash
# Check PostgreSQL status
pg_ctl status

# Start PostgreSQL
pg_ctl start

# Verify database exists
psql -l | grep locknroll
```

### Issue 3: Test Failures
```
Error: Tests failing with timeout
```
**Solution:**
- Ensure all services (Redis, PostgreSQL, MongoDB) are running
- Check if test databases exist
- Increase timeout values in test configuration

### Issue 4: Lock Timeout Errors
```
Error: Failed to acquire lock within timeout
```
**Solution:**
- This is expected behavior in high contention scenarios
- Increase lock timeout in `DistributedLockService`
- Reduce number of concurrent threads in tests

## 📖 Advanced Topics

### Customizing Cache Configuration

Edit `RedisConfig.java` to modify cache behavior:

```java
@Bean
public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // Increase TTL
            .serializeKeysWith(/* custom serialization */)
            .serializeValuesWith(/* custom serialization */);
    
    return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
}
```

### Customizing Lock Configuration

Edit `RedisConfig.java` to modify lock behavior:

```java
@Bean
public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()
            .setAddress("redis://localhost:6379")
            .setConnectionPoolSize(128) // Increase pool size
            .setConnectTimeout(15000)   // Increase timeout
            .setTimeout(5000);          // Increase operation timeout
    
    return Redisson.create(config);
}
```

### Adding Custom Metrics

Add custom metrics to monitor application behavior:

```java
@Component
public class FruitMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter cacheHits;
    private final Counter cacheMisses;
    
    public FruitMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.cacheHits = Counter.builder("fruit.cache.hits").register(meterRegistry);
        this.cacheMisses = Counter.builder("fruit.cache.misses").register(meterRegistry);
    }
    
    public void recordCacheHit() {
        cacheHits.increment();
    }
    
    public void recordCacheMiss() {
        cacheMisses.increment();
    }
}
```

## 🎯 Next Steps

1. **Extend the Application**: Add more complex business logic
2. **Add More Test Scenarios**: Create additional concurrency tests
3. **Implement Monitoring**: Add metrics and health checks
4. **Scale Testing**: Test with multiple application instances
5. **Performance Tuning**: Optimize Redis and database configurations

## 📚 Additional Resources

- [Spring Boot Cache Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.caching)
- [Redisson Documentation](https://github.com/redisson/redisson/wiki/Table-of-Content)
- [Redis Documentation](https://redis.io/documentation)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MongoDB Documentation](https://docs.mongodb.com/)

## 🤝 Contributing

Feel free to extend this project with:
- Additional test scenarios
- More complex business logic
- Performance optimizations
- Documentation improvements

Happy Learning! 🚀
