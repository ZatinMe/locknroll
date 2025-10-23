# LockNRoll Frontend - Modern React Application

A sophisticated React-based frontend for the LockNRoll State Management Platform, featuring real-time updates, role-based access control, and modern UI components.

## 🎯 Features

### Core Functionality
- **🔐 Authentication**: JWT-based login/register with secure token management
- **👥 Role-Based Access Control**: Different views and permissions for different user types
- **📊 Real-Time Dashboards**: Personalized dashboards with live updates via WebSocket
- **🍎 Fruit Management**: Complete CRUD operations for fruits with workflow integration
- **📋 Task Management**: Task assignment, completion, and dependency tracking
- **🔄 Workflow Management**: Start, monitor, and complete approval workflows
- **👤 User Management**: Admin interface for user management and role assignment
- **🔔 Real-Time Notifications**: WebSocket-based notifications for workflow events

### Advanced Features
- **⚡ Real-Time Updates**: Live dashboard updates via WebSocket connections
- **📱 Responsive Design**: Mobile-friendly interface with Material-UI
- **🎨 Modern UI**: Clean, intuitive interface with Material-UI components
- **🔄 State Management**: React Context for global state management
- **📊 Data Visualization**: Charts and graphs for workflow analytics
- **🔍 Search & Filter**: Advanced search and filtering capabilities
- **📄 Pagination**: Efficient data loading with pagination
- **🌙 Theme Support**: Light/dark theme support (future enhancement)

## 🛠️ Technology Stack

### Core Technologies
- **React 18**: Modern React with hooks and functional components
- **Material-UI (MUI)**: Comprehensive UI component library
- **React Router**: Client-side routing with protected routes
- **Axios**: HTTP client for API communication
- **React Hook Form**: Form handling with validation
- **React Hot Toast**: User-friendly notifications

### Development Tools
- **Create React App**: Development environment
- **ESLint**: Code linting and formatting
- **Prettier**: Code formatting
- **Jest**: Testing framework
- **React Testing Library**: Component testing

### State Management
- **React Context**: Global state management
- **useState/useEffect**: Local component state
- **Custom Hooks**: Reusable stateful logic
- **Local Storage**: Persistent user preferences

## 🚀 Getting Started

### Prerequisites

- **Node.js 16+** - JavaScript runtime
- **npm or yarn** - Package manager
- **Backend API** - LockNRoll backend running on port 8080

### Installation

1. **Install dependencies:**
```bash
npm install
```

2. **Start the development server:**
```bash
npm start
```

3. **Open [http://localhost:3000](http://localhost:3000) to view it in the browser.**

### Available Scripts

- `npm start` - Runs the app in development mode
- `npm build` - Builds the app for production
- `npm test` - Launches the test runner
- `npm eject` - Ejects from Create React App (irreversible)

## 👥 User Personas & Features

### 🔐 Admin
- **Full System Access**: Complete system administration
- **User Management**: Create, update, and manage users
- **Workflow Configuration**: Set up and modify approval workflows
- **System Monitoring**: View system statistics and performance
- **Role Management**: Assign and modify user roles
- **Audit Trails**: View complete system audit logs

### 🏢 Back Office
- **User Management**: Manage user accounts and permissions
- **Workflow Monitoring**: Oversee all active workflows
- **Task Oversight**: Monitor task completion and dependencies
- **Dashboard Analytics**: View system-wide statistics
- **Performance Metrics**: Track workflow efficiency
- **Reporting**: Generate system reports

### 👨‍💼 Manager
- **Task Assignment**: Assign tasks to team members
- **Workflow Approval**: Approve or reject workflow steps
- **Team Oversight**: Monitor team performance and workload
- **Performance Metrics**: Track team and individual performance
- **Workflow Monitoring**: Oversee team workflows
- **Resource Management**: Manage team resources

### 💰 Finance/Quality Approvers
- **Task Completion**: Complete assigned approval tasks
- **Approval Decisions**: Make approval/rejection decisions
- **Workflow Participation**: Participate in approval workflows
- **Status Updates**: Update task and workflow status
- **Documentation**: Review and approve documents
- **Compliance**: Ensure regulatory compliance

### 🍎 Seller
- **Fruit Management**: Create, update, and manage fruit listings
- **Workflow Submission**: Submit fruits for approval workflows
- **Task Completion**: Complete assigned tasks
- **Status Tracking**: Track application and workflow status
- **Dashboard**: View personal dashboard with tasks and workflows
- **Notifications**: Receive real-time notifications

## 🔌 API Integration

### Authentication Endpoints
- `POST /api/auth/login` - User authentication
- `POST /api/auth/register` - User registration
- `GET /api/auth/me` - Get current user info

### Dashboard Endpoints
- `GET /api/dashboard` - Current user dashboard
- `GET /api/dashboard/admin` - Admin dashboard
- `GET /api/dashboard/seller` - Seller dashboard
- `GET /api/dashboard/approver` - Approver dashboard

### Task Management
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/assigned/{userId}` - Get user's tasks
- `PUT /api/tasks/{taskId}/status` - Update task status
- `GET /api/tasks/ready/{workflowInstanceId}` - Get ready tasks
- `GET /api/tasks/blocked/{workflowInstanceId}` - Get blocked tasks

### Workflow Management
- `GET /api/workflows` - Get all workflows
- `POST /api/workflows` - Create workflow
- `GET /api/workflow-instances` - Get workflow instances
- `POST /api/workflow-instances` - Create workflow instance

### Fruit Management
- `GET /api/fruits` - Get all fruits
- `POST /api/fruits` - Create fruit
- `PUT /api/fruits/{id}` - Update fruit
- `DELETE /api/fruits/{id}` - Delete fruit
- `GET /api/fruits/workflow/draft` - Get draft fruits
- `POST /api/fruits/workflow/{id}/submit` - Submit for approval

### User Management
- `GET /api/users` - Get all users
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### WebSocket Endpoints
- `/ws/notifications/{username}` - User-specific notifications
- `/ws/notifications/role/{role}` - Role-based notifications
- `/ws/notifications/broadcast` - System-wide notifications

## 🎨 UI Components

### Layout Components
- **Navbar**: Navigation bar with user menu and notifications
- **Sidebar**: Collapsible sidebar for navigation
- **Footer**: Application footer with links and information
- **Layout**: Main application layout wrapper

### Dashboard Components
- **Dashboard**: Main dashboard with widgets and charts
- **StatsCard**: Statistics display cards
- **Chart**: Data visualization components
- **Table**: Data table with sorting and filtering

### Task Components
- **TaskList**: List of tasks with status indicators
- **TaskCard**: Individual task display card
- **TaskForm**: Task creation and editing form
- **TaskStatus**: Task status indicator with transitions

### Workflow Components
- **WorkflowList**: List of workflows with status
- **WorkflowCard**: Individual workflow display
- **WorkflowForm**: Workflow creation and editing
- **WorkflowStatus**: Workflow status with progress

### Fruit Components
- **FruitList**: List of fruits with filtering
- **FruitCard**: Individual fruit display card
- **FruitForm**: Fruit creation and editing form
- **FruitStatus**: Fruit status with workflow integration

### User Components
- **UserList**: List of users with role indicators
- **UserCard**: Individual user display card
- **UserForm**: User creation and editing form
- **RoleSelector**: Role selection component

### Notification Components
- **NotificationList**: List of notifications
- **NotificationCard**: Individual notification display
- **NotificationBadge**: Notification count badge
- **Toast**: Toast notification component

## 🔧 Configuration

### Environment Variables
```bash
# API Configuration
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_WS_URL=ws://localhost:8080

# Feature Flags
REACT_APP_ENABLE_NOTIFICATIONS=true
REACT_APP_ENABLE_REAL_TIME=true
REACT_APP_ENABLE_ANALYTICS=true

# Theme Configuration
REACT_APP_THEME=light
REACT_APP_PRIMARY_COLOR=#1976d2
```

### API Configuration
```javascript
// API base configuration
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
const WS_URL = process.env.REACT_APP_WS_URL || 'ws://localhost:8080';

// Axios configuration
axios.defaults.baseURL = API_BASE_URL;
axios.defaults.headers.common['Content-Type'] = 'application/json';
```

### WebSocket Configuration
```javascript
// WebSocket connection configuration
const wsConfig = {
  url: WS_URL,
  reconnectInterval: 5000,
  maxReconnectAttempts: 5,
  heartbeatInterval: 30000
};
```

## 🧪 Testing

### Unit Testing
```bash
# Run unit tests
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests in watch mode
npm test -- --watch
```

### Component Testing
```bash
# Test specific components
npm test -- --testNamePattern="TaskCard"

# Test with verbose output
npm test -- --verbose
```

### Integration Testing
```bash
# Test API integration
npm test -- --testNamePattern="API"

# Test WebSocket integration
npm test -- --testNamePattern="WebSocket"
```

## 📱 Responsive Design

### Breakpoints
- **Mobile**: < 600px
- **Tablet**: 600px - 960px
- **Desktop**: > 960px

### Responsive Features
- **Mobile Navigation**: Collapsible navigation menu
- **Touch Support**: Touch-friendly buttons and interactions
- **Responsive Tables**: Horizontal scrolling for small screens
- **Adaptive Layout**: Layout adjusts to screen size

## 🎨 Theming

### Material-UI Theme
```javascript
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
});
```

### Custom Styling
- **CSS Modules**: Component-specific styling
- **Styled Components**: Dynamic styling
- **Material-UI**: Consistent design system
- **Responsive Design**: Mobile-first approach

## 🚀 Deployment

### Production Build
```bash
# Create production build
npm run build

# Serve production build locally
npm install -g serve
serve -s build
```

### Environment Configuration
```bash
# Production environment
NODE_ENV=production
REACT_APP_API_BASE_URL=https://api.locknroll.com
REACT_APP_WS_URL=wss://api.locknroll.com
```

### Docker Deployment
```dockerfile
# Dockerfile for production
FROM node:16-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

## 🔧 Development

### Code Structure
```
src/
├── components/          # Reusable UI components
├── pages/              # Page components
├── contexts/           # React contexts
├── hooks/              # Custom hooks
├── services/           # API services
├── utils/              # Utility functions
├── styles/             # Global styles
└── tests/              # Test files
```

### Best Practices
- **Component Structure**: Functional components with hooks
- **State Management**: Context for global state, local state for components
- **API Integration**: Centralized API service layer
- **Error Handling**: Comprehensive error boundaries
- **Performance**: React.memo and useMemo for optimization
- **Accessibility**: ARIA labels and keyboard navigation

## 📚 Additional Resources

- [React Documentation](https://reactjs.org/docs/)
- [Material-UI Documentation](https://mui.com/)
- [React Router Documentation](https://reactrouter.com/)
- [Axios Documentation](https://axios-http.com/)
- [React Hook Form Documentation](https://react-hook-form.com/)

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new features
5. Submit a pull request

### Code Standards
- **ESLint**: Follow ESLint rules
- **Prettier**: Use Prettier for formatting
- **Testing**: Write tests for new features
- **Documentation**: Update documentation for changes

---

**Happy Building! 🚀**

*This frontend application provides a modern, responsive interface for the LockNRoll State Management Platform, featuring real-time updates, role-based access control, and comprehensive workflow management capabilities.*