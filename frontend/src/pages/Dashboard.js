import React from 'react';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  Chip,
  CircularProgress,
  Alert,
  Skeleton,
  Fade,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  ShoppingCart as FruitsIcon,
  Assignment as TasksIcon,
  AccountTree as WorkflowsIcon,
  TrendingUp as TrendingUpIcon,
  Error as ErrorIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useQuery } from 'react-query';
import axios from 'axios';
import { useAuth } from '../contexts/AuthContext';

const Dashboard = () => {
  const { user } = useAuth();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  const { data: dashboardData, isLoading, error, refetch } = useQuery(
    'dashboard',
    () => axios.get('/api/dashboard').then(res => res.data),
    {
      refetchInterval: 30000, // Refetch every 30 seconds
      retry: 3,
      retryDelay: 1000,
    }
  );

  if (isLoading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
          <Box textAlign="center">
            <CircularProgress size={60} />
            <Typography variant="h6" sx={{ mt: 2 }}>
              Loading your dashboard...
            </Typography>
          </Box>
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert 
          severity="error" 
          icon={<ErrorIcon />}
          action={
            <Box display="flex" gap={1}>
              <RefreshIcon 
                onClick={() => refetch()} 
                sx={{ cursor: 'pointer' }}
              />
            </Box>
          }
        >
          <Typography variant="h6" gutterBottom>
            Failed to load dashboard
          </Typography>
          <Typography variant="body2">
            {error.response?.data?.message || error.message}
          </Typography>
        </Alert>
      </Container>
    );
  }

  const stats = dashboardData?.statistics || {};
  const recentActivities = dashboardData?.recentActivities || [];
  const pendingTasks = dashboardData?.pendingTasks || [];

  const StatCard = ({ title, value, icon, color = 'primary' }) => (
    <Card sx={{ 
      height: '100%', 
      transition: 'all 0.2s ease-in-out',
      '&:hover': { 
        transform: 'translateY(-4px)',
        boxShadow: 4
      }
    }}>
      <CardContent>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box>
            <Typography color="textSecondary" gutterBottom variant="body2">
              {title}
            </Typography>
            <Typography variant="h4" component="div" fontWeight="bold">
              {value}
            </Typography>
          </Box>
          <Box color={`${color}.main`} sx={{ fontSize: 40 }}>
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  return (
    <Fade in={true} timeout={500}>
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Box mb={4}>
          <Typography variant="h4" gutterBottom>
            Welcome back, {user?.fullName}!
          </Typography>
          <Typography variant="subtitle1" color="textSecondary" gutterBottom>
            Here's what's happening with your account today.
          </Typography>
        </Box>
        
        <Grid container spacing={3}>
        {/* Statistics Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Fruits"
            value={stats.totalFruits || 0}
            icon={<FruitsIcon />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Pending Tasks"
            value={stats.pendingTasks || 0}
            icon={<TasksIcon />}
            color="warning"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Workflows"
            value={stats.activeWorkflows || 0}
            icon={<WorkflowsIcon />}
            color="info"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Completed Tasks"
            value={stats.completedTasks || 0}
            icon={<TrendingUpIcon />}
            color="success"
          />
        </Grid>

        {/* Recent Activities */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Recent Activities
            </Typography>
            <List>
              {recentActivities.length > 0 ? (
                recentActivities.map((activity, index) => (
                  <ListItem key={index}>
                    <ListItemText primary={activity} />
                  </ListItem>
                ))
              ) : (
                <ListItem>
                  <ListItemText primary="No recent activities" />
                </ListItem>
              )}
            </List>
          </Paper>
        </Grid>

        {/* Pending Tasks */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Pending Tasks
            </Typography>
            <List>
              {pendingTasks.length > 0 ? (
                pendingTasks.map((task, index) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={task.title || task.stepName}
                      secondary={task.description}
                    />
                    <Chip
                      label={task.status}
                      size="small"
                      color={task.status === 'PENDING' ? 'warning' : 'default'}
                    />
                  </ListItem>
                ))
              ) : (
                <ListItem>
                  <ListItemText primary="No pending tasks" />
                </ListItem>
              )}
            </List>
          </Paper>
        </Grid>

        {/* User Info */}
        <Grid item xs={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Account Information
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Username
                </Typography>
                <Typography variant="body1">
                  {user?.username}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Email
                </Typography>
                <Typography variant="body1">
                  {user?.email}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Full Name
                </Typography>
                <Typography variant="body1">
                  {user?.fullName}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="textSecondary">
                  Roles
                </Typography>
                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  {user?.roles?.map((role) => (
                    <Chip
                      key={role}
                      label={role.replace('_ROLE', '')}
                      size="small"
                      color="primary"
                      variant="outlined"
                    />
                  ))}
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Container>
    </Fade>
  );
};

export default Dashboard;
