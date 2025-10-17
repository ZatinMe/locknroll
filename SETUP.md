# Setup Guide - Redis Learning Project

This guide will help you set up the complete development environment for the Redis Learning Project.

## 📋 Prerequisites Checklist

Before starting, ensure you have the following installed:

- [ ] Java 17 or higher
- [ ] Maven 3.6 or higher
- [ ] PostgreSQL 13 or higher
- [ ] MongoDB 4.4 or higher
- [ ] Redis 6.0 or higher
- [ ] Git (optional, for version control)

## 🛠️ Installation Steps

### 1. Java Installation

**macOS (using Homebrew):**
```bash
brew install openjdk@17
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**Windows:**
- Download OpenJDK 17 from [Adoptium](https://adoptium.net/)
- Follow the installation wizard
- Set JAVA_HOME environment variable

**Verify Installation:**
```bash
java -version
javac -version
```

### 2. Maven Installation

**macOS (using Homebrew):**
```bash
brew install maven
```

**Ubuntu/Debian:**
```bash
sudo apt install maven
```

**Windows:**
- Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
- Extract to a directory (e.g., `C:\apache-maven-3.9.0`)
- Add `C:\apache-maven-3.9.0\bin` to your PATH

**Verify Installation:**
```bash
mvn -version
```

### 3. PostgreSQL Installation

**macOS (using Homebrew):**
```bash
brew install postgresql@13
brew services start postgresql@13
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

**Windows:**
- Download PostgreSQL from [postgresql.org](https://www.postgresql.org/download/windows/)
- Run the installer
- Remember the password you set for the `postgres` user

**Create Databases:**
```bash
# Connect to PostgreSQL
psql -U postgres

# Create databases
CREATE DATABASE locknroll_db;
CREATE DATABASE locknroll_test_db;

# Exit PostgreSQL
\q
```

**Verify Installation:**
```bash
psql -U postgres -c "SELECT version();"
```

### 4. MongoDB Installation

**macOS (using Homebrew):**
```bash
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb/brew/mongodb-community
```

**Ubuntu/Debian:**
```bash
# Import MongoDB public key
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -

# Create list file
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list

# Install MongoDB
sudo apt update
sudo apt install mongodb-org
sudo systemctl start mongod
sudo systemctl enable mongod
```

**Windows:**
- Download MongoDB from [mongodb.com](https://www.mongodb.com/try/download/community)
- Run the installer
- Choose "Complete" installation
- Install MongoDB as a Windows Service

**Verify Installation:**
```bash
mongosh --version
# or
mongo --version
```

### 5. Redis Installation

**macOS (using Homebrew):**
```bash
brew install redis
brew services start redis
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

**Windows:**
- Download Redis from [Redis for Windows](https://github.com/microsoftarchive/redis/releases)
- Or use WSL2 with Ubuntu and install Redis there
- Or use Docker: `docker run -d -p 6379:6379 redis:alpine`

**Verify Installation:**
```bash
redis-cli ping
# Should return: PONG
```

## 🔧 Configuration

### 1. Database Configuration

**PostgreSQL Configuration:**
```bash
# Edit PostgreSQL configuration (optional)
sudo nano /etc/postgresql/13/main/postgresql.conf

# Ensure these settings:
# listen_addresses = 'localhost'
# port = 5432
# max_connections = 100
```

**MongoDB Configuration:**
```bash
# MongoDB configuration is usually fine with defaults
# Default port: 27017
# Default bind: 127.0.0.1
```

**Redis Configuration:**
```bash
# Edit Redis configuration (optional)
sudo nano /etc/redis/redis.conf

# Ensure these settings:
# bind 127.0.0.1
# port 6379
# timeout 0
# tcp-keepalive 300
```

### 2. Application Configuration

The application configuration is already set up in `src/main/resources/application.yml`. You may need to adjust the following if your services run on different ports:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/locknroll_db
    username: postgres
    password: postgres  # Change this to your PostgreSQL password
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/locknroll_mongo
    
    redis:
      host: localhost
      port: 6379
      password:  # Add password if Redis is password-protected
```

## 🚀 Running the Application

### 1. Start All Services

**Start PostgreSQL:**
```bash
# macOS
brew services start postgresql@13

# Ubuntu/Debian
sudo systemctl start postgresql

# Windows
# PostgreSQL should start automatically as a service
```

**Start MongoDB:**
```bash
# macOS
brew services start mongodb/brew/mongodb-community

# Ubuntu/Debian
sudo systemctl start mongod

# Windows
# MongoDB should start automatically as a service
```

**Start Redis:**
```bash
# macOS
brew services start redis

# Ubuntu/Debian
sudo systemctl start redis-server

# Windows
# Redis should start automatically if installed as a service
```

### 2. Verify Services

```bash
# Check PostgreSQL
psql -U postgres -c "SELECT 1;"

# Check MongoDB
mongosh --eval "db.runCommand('ping')"

# Check Redis
redis-cli ping
```

### 3. Build and Run Application

```bash
# Navigate to project directory
cd lockNroll

# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

### 4. Verify Application

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Get sample data
curl http://localhost:8080/api/fruits
```

## 🧪 Running Tests

### 1. Run All Tests

```bash
mvn test
```

### 2. Run Specific Tests

```bash
# Run concurrency tests
mvn test -Dtest=FruitServiceConcurrencyTest

# Run integration tests
mvn test -Dtest=RedisIntegrationTest

# Run specific test method
mvn test -Dtest=FruitServiceConcurrencyTest#testConcurrentPurchases
```

### 3. Run Tests with Verbose Output

```bash
mvn test -X
```

## 🔍 Troubleshooting

### Common Issues

**1. Port Already in Use**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

**2. Database Connection Failed**
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Check PostgreSQL logs
sudo tail -f /var/log/postgresql/postgresql-13-main.log
```

**3. Redis Connection Failed**
```bash
# Check if Redis is running
redis-cli ping

# Check Redis logs
sudo tail -f /var/log/redis/redis-server.log
```

**4. MongoDB Connection Failed**
```bash
# Check if MongoDB is running
sudo systemctl status mongod

# Check MongoDB logs
sudo tail -f /var/log/mongodb/mongod.log
```

**5. Maven Build Failed**
```bash
# Clean and rebuild
mvn clean
mvn compile

# Check Java version
java -version
mvn -version
```

### Service-Specific Troubleshooting

**PostgreSQL:**
```bash
# Reset PostgreSQL password
sudo -u postgres psql
ALTER USER postgres PASSWORD 'newpassword';
\q

# Create databases manually
createdb -U postgres locknroll_db
createdb -U postgres locknroll_test_db
```

**MongoDB:**
```bash
# Connect to MongoDB
mongosh

# Create databases manually
use locknroll_mongo
use locknroll_test_mongo
```

**Redis:**
```bash
# Connect to Redis
redis-cli

# Check Redis info
INFO

# Clear all data (if needed)
FLUSHALL
```

## 📊 Monitoring Setup

### 1. Application Logs

```bash
# View application logs
tail -f logs/redis-learning.log

# Filter logs
grep "Thread" logs/redis-learning.log
grep "lock" logs/redis-learning.log
```

### 2. Database Monitoring

**PostgreSQL:**
```sql
-- Connect to database
psql -U postgres -d locknroll_db

-- Check active connections
SELECT * FROM pg_stat_activity;

-- Check database size
SELECT pg_size_pretty(pg_database_size('locknroll_db'));
```

**MongoDB:**
```javascript
// Connect to MongoDB
mongosh

// Check database stats
db.stats()

// Check collections
show collections

// Check transaction logs
db.fruit_transactions.find().limit(5)
```

**Redis:**
```bash
# Connect to Redis
redis-cli

# Check Redis info
INFO

# Monitor Redis commands
MONITOR

# Check memory usage
INFO memory
```

## 🎯 Next Steps

1. **Run the Application**: Follow the steps above to get everything running
2. **Explore the Code**: Start with `FruitService.java` and `DistributedLockService.java`
3. **Run Tests**: Execute the concurrency tests to see Redis in action
4. **Experiment**: Try modifying the code and see how it affects behavior
5. **Monitor**: Use the monitoring tools to understand what's happening

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Redis Documentation](https://redis.io/documentation)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Maven Documentation](https://maven.apache.org/guides/)

## 🆘 Getting Help

If you encounter issues:

1. Check the logs for error messages
2. Verify all services are running
3. Check the troubleshooting section above
4. Review the application configuration
5. Ensure all prerequisites are installed correctly

Happy Learning! 🚀
