# LockN'Roll - Enterprise State Management Platform

A sophisticated Spring Boot application implementing a complete **event-driven state management platform** with configurable approval workflows, JWT authentication, role-based access control, and real-time workflow orchestration for fruit management systems.

## 🎯 Project Overview

This project demonstrates a **production-ready enterprise state management platform** with advanced workflow orchestration capabilities:

- **🔐 JWT Authentication & Authorization** - Secure user authentication with role-based access control
- **📋 Event-Driven Workflow Orchestration** - Real-time workflow progression with automatic dependency resolution
- **👥 Multi-Persona User Management** - Role-based user system (Admin, BackOffice, Seller, Approvers)
- **📊 Real-Time Dashboards** - Personalized dashboards with live updates via WebSocket
- **🔄 Advanced Task Management** - Task assignment with complex dependency tracking and automatic progression
- **📈 State Management** - Entity state transitions with comprehensive audit trails
- **🗄️ Multi-Database Architecture** - PostgreSQL for entities, MongoDB for transactions, Redis for caching
- **⚡ Real-Time Notifications** - WebSocket-based notifications for workflow events
- **🎭 React Frontend** - Modern React application with Material-UI components

## 🏗️ Architecture

### Event-Driven Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                    Event-Driven Workflow Engine                │
├─────────────────────────────────────────────────────────────────┤
│  Task Completion → Event Publisher → Kafka → Event Listener    │
│       ↓                ↓              ↓           ↓           │
│  Update Dependencies → Notify Users → Update Cache → Dashboard │
└─────────────────────────────────────────────────────────────────┘
```

### Multi-Database Architecture
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
│   + WebSocket   │    │   & Security    │    │   (Transactions)│
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Frontend │    │   Kafka Events  │    │   Event Store   │
│   (Material-UI)  │    │   (Orchestration)│   │   (Audit Trail) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- **Java 17+** - OpenJDK or Oracle JDK
- **Maven 3.6+** - Build tool
- **PostgreSQL 13+** - Primary database
- **MongoDB 4.4+** - Transaction store
- **Redis 6.0+** - Caching and sessions
- **Node.js 16+** - Frontend development
- **Kafka** (Optional) - Event streaming

### 1. Database Setup

#### PostgreSQL Setup
```bash
# Create databases
createdb locknroll_db
createdb locknroll_test_db

# Verify connection
psql -U postgres -c "SELECT version();"
```

#### MongoDB Setup
```bash
# MongoDB will create databases automatically
# Verify connection
mongosh --eval "db.runCommand('ping')"
```

#### Redis Setup
```bash
# Install Redis (macOS)
brew install redis
brew services start redis

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

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

The frontend will start on `http://localhost:3000`

### 4. Verify Setup

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "password123"}'
```

## 🔐 Authentication & Authorization

### Default Users

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| `admin` | `password123` | ADMIN_ROLE | System administrator |
| `backoffice` | `password123` | BACKOFFICE_ROLE | Back office operations |
| `seller1` | `password123` | SELLER_ROLE | Fruit seller |
| `finance1` | `password123` | FINANCE_APPROVER_ROLE | Finance approver |
| `quality1` | `password123` | QUALITY_APPROVER_ROLE | Quality approver |
| `manager1` | `password123` | MANAGER_APPROVER_ROLE | Manager approver |

### Authentication Flow

```bash
# 1. Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "password123"}'

# 2. Use token in subsequent requests
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/dashboard
```

## 🎭 User Personas & Dashboards

### 1. Admin/BackOffice Dashboard
- **Overview**: System-wide statistics and management
- **Features**: 
  - All pending tasks across the system
  - Active workflow instances with real-time updates
  - User management and role assignment
  - System statistics and performance metrics
  - Event monitoring and audit trails

### 2. Seller Dashboard
- **Overview**: Personal fruit management and tracking
- **Features**:
  - My submitted fruits with real-time status updates
  - Application status tracking with dependency visualization
  - Fruit status counts (Draft, Pending, Approved, Rejected)
  - Task notifications and workflow progression

### 3. Approver Dashboard
- **Overview**: Task management and approval workflow
- **Features**:
  - Assigned tasks with dependency information
  - Pending approvals with priority indicators
  - Workflow instance tracking with real-time updates
  - Approval history and audit trails

## 🔄 Event-Driven Workflow System

### Workflow Orchestration

The system features **sophisticated event-driven workflow orchestration**:

- **Automatic Progression**: Tasks automatically progress when dependencies are satisfied
- **Real-Time Updates**: Users receive immediate notifications when tasks become available
- **Dependency Resolution**: Complex multi-level dependencies are handled automatically
- **Failure Handling**: Critical rejections trigger workflow cancellation with proper cleanup

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
| `READY` | Dependencies satisfied, ready to start |
| `IN_PROGRESS` | Task being worked on |
| `COMPLETED` | Task completed successfully |
| `REJECTED` | Task rejected |
| `BLOCKED` | Task blocked by dependencies |
| `CANCELLED` | Task cancelled |

### Event-Driven Flow

```
Task A (Finance) → COMPLETED
       ↓
   [Event Published to Kafka]
       ↓
   [WorkflowEventListener processes event]
       ↓
   [updateDependentTasksFromEvent() called]
       ↓
Task B (Quality) → READY (dependencies satisfied)
       ↓
   [User notified via WebSocket]
       ↓
   [Dashboard updated in real-time]
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
- `GET /api/tasks/ready/{workflowInstanceId}` - Get ready tasks
- `GET /api/tasks/blocked/{workflowInstanceId}` - Get blocked tasks

**Example:**
```bash
# Get tasks assigned to current user
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/tasks/assigned/1

# Update task status (triggers event-driven workflow progression)
curl -X PUT http://localhost:8080/api/tasks/1/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "COMPLETED", "comments": "Task completed successfully"}'
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

### 6. Real-Time Notifications

**WebSocket Endpoints:**
- `/ws/notifications/{username}` - User-specific notifications
- `/ws/notifications/role/{role}` - Role-based notifications
- `/ws/notifications/broadcast` - System-wide notifications

## 🧪 Testing the System

### 1. Complete Workflow Test

```bash
# 1. Login as seller
SELLER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "seller1", "password": "password123"}' | \
  jq -r '.accessToken')

# 2. Submit fruit for approval
curl -X POST http://localhost:8080/api/fruits/workflow/1/submit \
  -H "Authorization: Bearer $SELLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"submittedBy": "seller1", "comments": "Ready for approval"}'

# 3. Login as finance approver
FINANCE_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "finance1", "password": "password123"}' | \
  jq -r '.accessToken')

# 4. Get assigned tasks
curl -H "Authorization: Bearer $FINANCE_TOKEN" \
  http://localhost:8080/api/tasks/assigned/4

# 5. Approve task (triggers event-driven workflow progression)
curl -X PUT http://localhost:8080/api/tasks/1/status \
  -H "Authorization: Bearer $FINANCE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "COMPLETED", "comments": "Finance approval completed"}'

# 6. Check that dependent tasks are now ready
curl -H "Authorization: Bearer $FINANCE_TOKEN" \
  http://localhost:8080/api/tasks/ready/1
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
  -d '{"usernameOrEmail": "admin", "password": "password123"}' | \
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
tail -f logs/redis-learning.log

# Filter for specific operations
grep "Event-driven" logs/redis-learning.log
grep "Workflow" logs/redis-learning.log
grep "Task" logs/redis-learning.log
grep "Authentication" logs/redis-learning.log
```

### Database Queries

**PostgreSQL:**
```sql
-- Check workflow instances
SELECT * FROM workflow_instances ORDER BY created_at DESC;

-- Check tasks by status
SELECT * FROM tasks WHERE status = 'READY';

-- Check user roles
SELECT u.username, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id;

-- Check task dependencies
SELECT t.title, td.parent_task_id, pt.title as parent_title
FROM tasks t
JOIN task_dependencies td ON t.id = td.dependent_task_id
JOIN tasks pt ON td.parent_task_id = pt.id;
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
- Verify user credentials (use `password123` not `admin123`)

### Issue 2: Permission Denied
```
Error: 403 Forbidden
```
**Solution:**
- Check user roles and permissions
- Ensure user has required role for the operation
- Verify workflow step assignments

### Issue 3: Task Dependencies Not Met
```
Error: Task dependencies not satisfied
```
**Solution:**
- Complete parent tasks first
- Check task dependency configuration
- Verify workflow step dependencies

### Issue 4: Event Processing Issues
```
Error: Event listener not processing events
```
**Solution:**
- Check Kafka configuration
- Verify event publisher is working
- Check application logs for event processing errors

## 🎯 Project Status

### ✅ Completed Features

1. **✅ Core Entities** - User, Role, Workflow, Task, Approval entities
2. **✅ User Management** - Authentication, authorization, role-based access
3. **✅ Workflow Configuration** - Configurable approval chains
4. **✅ Task Management** - Task assignment with dependencies
5. **✅ State Management** - Entity state transitions
6. **✅ JWT Authentication** - Secure token-based authentication
7. **✅ RBAC System** - Role-based access control
8. **✅ Dashboard APIs** - Personalized dashboards for all user types
9. **✅ Event-Driven Architecture** - Real-time workflow orchestration
10. **✅ Dependency Resolution** - Automatic task progression
11. **✅ Real-Time Notifications** - WebSocket-based notifications
12. **✅ React Frontend** - Modern UI with Material-UI
13. **✅ Multi-Database Architecture** - PostgreSQL, MongoDB, Redis
14. **✅ Caching System** - Redis-based caching
15. **✅ Audit Trail** - Comprehensive logging and event tracking

### 🚧 Optional Enhancements

- **Advanced Analytics** - Workflow performance metrics
- **Mobile App** - React Native mobile application
- **API Documentation** - Swagger/OpenAPI documentation
- **Load Testing** - Performance testing suite
- **Docker Deployment** - Containerized deployment
- **CI/CD Pipeline** - Automated testing and deployment

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
- `GET /api/tasks/ready/{workflowInstanceId}` - Get ready tasks
- `GET /api/tasks/blocked/{workflowInstanceId}` - Get blocked tasks

### Dashboard APIs
- `GET /api/dashboard` - Current user dashboard
- `GET /api/dashboard/admin` - Admin dashboard
- `GET /api/dashboard/seller` - Seller dashboard
- `GET /api/dashboard/approver` - Approver dashboard

### Fruit Workflow
- `GET /api/fruits/workflow/draft` - Get draft fruits
- `POST /api/fruits/workflow/{id}/submit` - Submit for approval
- `GET /api/fruits/workflow/pending` - Get pending fruits

### WebSocket Endpoints
- `/ws/notifications/{username}` - User notifications
- `/ws/notifications/role/{role}` - Role notifications
- `/ws/notifications/broadcast` - System notifications

## 🤝 Contributing

This project demonstrates enterprise-level patterns and can be extended with:

- **Advanced Workflows** - Complex approval scenarios
- **Performance Optimization** - Caching strategies and database optimization
- **Monitoring** - Metrics and health checks
- **Testing** - Comprehensive test coverage
- **Documentation** - API documentation and user guides

## 📖 Additional Resources

- [Spring Boot Security Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-security)
- [JWT.io](https://jwt.io/) - JWT token debugging
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Redis Documentation](https://redis.io/documentation)
- [React Documentation](https://reactjs.org/docs/)
- [Material-UI Documentation](https://mui.com/)

---

**Happy Building! 🚀**

*This project demonstrates a complete enterprise state management platform with event-driven workflow orchestration, real-time notifications, and sophisticated dependency resolution - showcasing advanced Spring Boot development patterns and modern web technologies.*