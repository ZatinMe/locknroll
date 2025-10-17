# LockN'Roll - State Management Platform

A comprehensive Spring Boot application implementing a state management platform with configurable approval workflows, JWT authentication, and role-based access control for fruit management system.

## 🎯 Project Overview

This project demonstrates a complete state management platform with:

- **🔐 JWT Authentication & Authorization** - Secure user authentication with role-based access control
- **📋 Configurable Approval Workflows** - Multi-tier approval processes with dependencies
- **👥 User Management** - Role-based user system (Admin, BackOffice, Seller, Approvers)
- **📊 Dashboard APIs** - Personalized dashboards for different user personas
- **🔄 Task Management** - Task assignment with dependency tracking
- **📈 State Management** - Entity state transitions with audit trails
- **🗄️ Multi-Database Architecture** - PostgreSQL for entities, MongoDB for transactions, Redis for caching

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │    │      Redis      │    │   PostgreSQL    │
│   Application   │◄──►│   (Cache +      │    │   (Entities)    │
│                 │    │    Sessions)    │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   JWT Tokens    │    │   MongoDB       │
│   Endpoints     │    │   & Security    │    │   (Transactions)│
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

## 🔐 Authentication & Authorization

### Default Users

The application initializes with the following default users:

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| `admin` | `admin123` | ADMIN_ROLE | System administrator |
| `backoffice` | `backoffice123` | BACKOFFICE_ROLE | Back office operations |
| `seller1` | `seller123` | SELLER_ROLE | Fruit seller |
| `finance1` | `finance123` | FINANCE_APPROVER_ROLE | Finance approver |
| `quality1` | `quality123` | QUALITY_APPROVER_ROLE | Quality approver |
| `manager1` | `manager123` | MANAGER_APPROVER_ROLE | Manager approver |

### Authentication Flow

```bash
# 1. Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "seller1", "password": "seller123"}'

# 2. Use token in subsequent requests
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/dashboard
```

## 📋 Core Features

### 1. User Management

**Endpoints:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/users` - Get all users (Admin only)
- `POST /api/users` - Create user (Admin only)

**Example:**
```bash
# Register a new seller
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newseller",
    "email": "newseller@example.com",
    "password": "password123",
    "firstName": "New",
    "lastName": "Seller"
  }'
```

### 2. Workflow Management

**Endpoints:**
- `GET /api/workflows` - Get all workflows
- `POST /api/workflows` - Create workflow (Admin only)
- `GET /api/workflow-instances` - Get workflow instances
- `POST /api/workflow-instances` - Create workflow instance

**Example:**
```bash
# Get all workflows
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/workflows
```

### 3. Task Management

**Endpoints:**
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/assigned/{userId}` - Get tasks assigned to user
- `PUT /api/tasks/{taskId}/status` - Update task status

**Example:**
```bash
# Get tasks assigned to current user
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/tasks/assigned/1
```

### 4. Fruit Workflow Integration

**Endpoints:**
- `GET /api/fruits/workflow/draft` - Get draft fruits
- `POST /api/fruits/workflow/{fruitId}/submit` - Submit fruit for approval
- `GET /api/fruits/workflow/pending` - Get pending approval fruits

**Example:**
```bash
# Submit fruit for approval
curl -X POST http://localhost:8080/api/fruits/workflow/1/submit \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"submittedBy": "seller1", "comments": "Ready for approval"}'
```

### 5. Dashboard APIs

**Endpoints:**
- `GET /api/dashboard` - Get current user's dashboard
- `GET /api/dashboard/admin` - Admin dashboard
- `GET /api/dashboard/seller` - Seller dashboard
- `GET /api/dashboard/approver` - Approver dashboard

**Example:**
```bash
# Get seller dashboard
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/dashboard/seller
```

## 🎭 User Personas & Dashboards

### 1. Admin/BackOffice Dashboard
- **Overview**: System-wide statistics and management
- **Features**: 
  - All pending tasks across the system
  - Active workflow instances
  - User management
  - System statistics

### 2. Seller Dashboard
- **Overview**: Personal fruit management and tracking
- **Features**:
  - My submitted fruits
  - Application status tracking
  - Fruit status counts (Draft, Pending, Approved, Rejected)

### 3. Approver Dashboard
- **Overview**: Task management and approval workflow
- **Features**:
  - Assigned tasks
  - Pending approvals
  - Workflow instance tracking

## 🔄 Workflow System

### Workflow Configuration

The system supports configurable approval workflows with:

- **Multi-tier Approvals**: Finance → Quality → Manager
- **Dependencies**: Parent tasks must complete before dependent tasks
- **Role-based Assignment**: Tasks assigned to specific roles
- **Conditional Logic**: Support for different approval paths

### Workflow States

| State | Description |
|-------|-------------|
| `DRAFT` | Initial state, not submitted |
| `PENDING_APPROVAL` | Submitted, awaiting approval |
| `IN_PROGRESS` | Workflow in progress |
| `APPROVED` | All approvals completed |
| `REJECTED` | Rejected at any stage |
| `CANCELLED` | Workflow cancelled |

### Task States

| State | Description |
|-------|-------------|
| `PENDING` | Task created, not started |
| `IN_PROGRESS` | Task being worked on |
| `COMPLETED` | Task completed successfully |
| `REJECTED` | Task rejected |
| `CANCELLED` | Task cancelled |

## 🧪 Testing the System

### 1. Complete Workflow Test

```bash
# 1. Login as seller
SELLER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "seller1", "password": "seller123"}' | \
  jq -r '.accessToken')

# 2. Submit fruit for approval
curl -X POST http://localhost:8080/api/fruits/workflow/1/submit \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"submittedBy": "seller1", "comments": "Ready for approval"}'

# 3. Login as finance approver
FINANCE_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "finance1", "password": "finance123"}' | \
  jq -r '.accessToken')

# 4. Get assigned tasks
curl -H "Authorization: Bearer $FINANCE_TOKEN" \
  http://localhost:8080/api/tasks/assigned/4

# 5. Approve task
curl -X PUT http://localhost:8080/api/tasks/1/status \
  -H "Authorization: Bearer $FINANCE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "COMPLETED", "comments": "Finance approval completed"}'
```

### 2. Dashboard Testing

```bash
# Test different dashboard views
curl -H "Authorization: Bearer $SELLER_TOKEN" \
  http://localhost:8080/api/dashboard/seller

curl -H "Authorization: Bearer $FINANCE_TOKEN" \
  http://localhost:8080/api/dashboard/approver

# Admin dashboard (requires admin token)
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "admin123"}' | \
  jq -r '.accessToken')

curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/dashboard/admin
```

## 🗄️ Database Schema

### Core Entities

#### Users & Roles
- **users**: User accounts with authentication
- **roles**: System roles (Admin, Seller, Approvers)
- **permissions**: Granular permissions
- **user_roles**: Many-to-many user-role mapping

#### Workflow System
- **workflows**: Workflow templates
- **workflow_steps**: Individual steps in workflows
- **workflow_step_dependencies**: Dependencies between steps
- **workflow_instances**: Active workflow executions
- **tasks**: Individual work items
- **task_dependencies**: Task dependencies
- **approvals**: Approval decisions

#### Fruit Management
- **fruits**: Fruit entities with state management
- **fruit_transactions**: MongoDB transaction logs

## 🔍 Monitoring and Debugging

### Application Logs

```bash
# View application logs
tail -f logs/application.log

# Filter for specific operations
grep "Workflow" logs/application.log
grep "Task" logs/application.log
grep "Authentication" logs/application.log
```

### Database Queries

**PostgreSQL:**
```sql
-- Check workflow instances
SELECT * FROM workflow_instances ORDER BY created_at DESC;

-- Check tasks by status
SELECT * FROM tasks WHERE status = 'PENDING';

-- Check user roles
SELECT u.username, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id;
```

**MongoDB:**
```javascript
// Check transaction logs
db.fruit_transactions.find().sort({timestamp: -1}).limit(10);

// Count operations by type
db.fruit_transactions.aggregate([
  {$group: {_id: "$operationType", count: {$sum: 1}}}
]);
```

### Redis Monitoring

```bash
# Connect to Redis CLI
redis-cli

# Check session keys
KEYS spring:session:*

# Check cache keys
KEYS fruits:*

# Monitor Redis commands
MONITOR
```

## 🚨 Common Issues and Solutions

### Issue 1: Authentication Failed
```
Error: 401 Unauthorized
```
**Solution:**
- Ensure JWT token is included in Authorization header
- Check if token has expired
- Verify user credentials

### Issue 2: Permission Denied
```
Error: 403 Forbidden
```
**Solution:**
- Check user roles and permissions
- Ensure user has required role for the operation
- Verify workflow step assignments

### Issue 3: Workflow Instance Not Found
```
Error: Workflow instance not found
```
**Solution:**
- Ensure workflow instance exists for the entity
- Check entity type and ID mapping
- Verify workflow configuration

### Issue 4: Task Dependencies Not Met
```
Error: Task dependencies not satisfied
```
**Solution:**
- Complete parent tasks first
- Check task dependency configuration
- Verify workflow step dependencies

## 🎯 Next Steps

### Completed Phases ✅
1. ✅ **Core Entities** - User, Role, Workflow, Task, Approval entities
2. ✅ **User Management** - Authentication, authorization, role-based access
3. ✅ **Workflow Configuration** - Configurable approval chains
4. ✅ **Task Management** - Task assignment with dependencies
5. ✅ **State Management** - Entity state transitions
6. ✅ **JWT Authentication** - Secure token-based authentication
7. ✅ **RBAC System** - Role-based access control
8. ✅ **Dashboard APIs** - Personalized dashboards for all user types

### Upcoming Phases 🚧
9. **Task Assignment Logic** - Advanced task assignment algorithms
10. **Event-driven Notifications** - Real-time notifications for state changes
11. **Audit Trail System** - Comprehensive audit logging
12. **Seller Onboarding** - Complete seller registration workflow
13. **Fruit Submission Workflow** - End-to-end fruit approval process
14. **Frontend Implementation** - React/Vue.js frontend
15. **Approver Dashboards** - Enhanced approver interfaces
16. **Seller Application Tracking** - Real-time application status
17. **Back Office Admin Panel** - Complete admin interface
18. **Task Dependency Visualization** - Visual workflow representation
19. **Testing & Documentation** - Comprehensive test coverage

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### User Management
- `GET /api/users` - Get all users
- `POST /api/users` - Create user
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user

### Workflow Management
- `GET /api/workflows` - Get all workflows
- `POST /api/workflows` - Create workflow
- `GET /api/workflow-instances` - Get workflow instances
- `POST /api/workflow-instances` - Create workflow instance

### Task Management
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/assigned/{userId}` - Get user's tasks
- `PUT /api/tasks/{taskId}/status` - Update task status

### Dashboard APIs
- `GET /api/dashboard` - Current user dashboard
- `GET /api/dashboard/admin` - Admin dashboard
- `GET /api/dashboard/seller` - Seller dashboard
- `GET /api/dashboard/approver` - Approver dashboard

### Fruit Workflow
- `GET /api/fruits/workflow/draft` - Get draft fruits
- `POST /api/fruits/workflow/{id}/submit` - Submit for approval
- `GET /api/fruits/workflow/pending` - Get pending fruits

## 🤝 Contributing

This project demonstrates enterprise-level patterns and can be extended with:

- **Frontend Implementation** - React/Vue.js interfaces
- **Advanced Workflows** - Complex approval scenarios
- **Real-time Features** - WebSocket notifications
- **Performance Optimization** - Caching strategies
- **Monitoring** - Metrics and health checks
- **Testing** - Comprehensive test coverage

## 📖 Additional Resources

- [Spring Boot Security Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-security)
- [JWT.io](https://jwt.io/) - JWT token debugging
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Redis Documentation](https://redis.io/documentation)

---

**Happy Building! 🚀**

*This project demonstrates a complete state management platform with enterprise-level features including JWT authentication, role-based access control, configurable workflows, and comprehensive task management.*