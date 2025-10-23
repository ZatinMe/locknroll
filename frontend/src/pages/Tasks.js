import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  Card,
  CardContent,
  Chip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Tooltip,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Assignment as TaskIcon,
  CheckCircle as CompletedIcon,
  Pending as PendingIcon,
  Block as BlockedIcon,
  PlayArrow as StartIcon,
  Stop as StopIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import axios from 'axios';
import toast from 'react-hot-toast';
import { useAuth } from '../contexts/AuthContext';

const Tasks = () => {
  const { user } = useAuth();
  const [tabValue, setTabValue] = useState(0);
  const [selectedTask, setSelectedTask] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [action, setAction] = useState('');

  const queryClient = useQueryClient();

  const { data: tasks, isLoading, error } = useQuery(
    'tasks',
    () => {
      console.log('Fetching tasks for user ID:', user?.id);
      return axios.get(`/api/tasks/user/${user?.id}`).then(res => {
        console.log('Tasks API response:', res.data);
        return res.data;
      });
    },
    {
      enabled: !!user?.id,
      retry: 3,
      retryDelay: 1000,
    }
  );

  const updateTaskMutation = useMutation(
    ({ taskId, status, comment }) => 
      axios.put(`/api/tasks/${taskId}/status`, { status, comment }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('tasks');
        toast.success('Task updated successfully!');
        setOpenDialog(false);
      },
      onError: (error) => {
        toast.error('Failed to update task: ' + error.response?.data?.message);
      },
    }
  );

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleTaskAction = (task, actionType) => {
    setSelectedTask(task);
    setAction(actionType);
    setOpenDialog(true);
  };

  const handleSubmitAction = (comment = '') => {
    if (selectedTask) {
      updateTaskMutation.mutate({
        taskId: selectedTask.id,
        status: action === 'complete' ? 'COMPLETED' : action === 'reject' ? 'REJECTED' : 'IN_PROGRESS',
        comment,
      });
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      'PENDING': 'default',
      'READY': 'info',
      'IN_PROGRESS': 'primary',
      'COMPLETED': 'success',
      'REJECTED': 'error',
      'BLOCKED': 'warning',
      'CANCELLED': 'default',
    };
    return colors[status] || 'default';
  };

  const getStatusIcon = (status) => {
    const icons = {
      'PENDING': <PendingIcon />,
      'READY': <StartIcon />,
      'IN_PROGRESS': <StartIcon />,
      'COMPLETED': <CompletedIcon />,
      'REJECTED': <StopIcon />,
      'BLOCKED': <BlockedIcon />,
      'CANCELLED': <StopIcon />,
    };
    return icons[status] || <TaskIcon />;
  };

  const filteredTasks = tasks?.filter(task => {
    switch (tabValue) {
      case 0: return task.status === 'PENDING' || task.status === 'READY';
      case 1: return task.status === 'IN_PROGRESS';
      case 2: return task.status === 'COMPLETED';
      case 3: return task.status === 'BLOCKED';
      default: return true;
    }
  }) || [];

  if (isLoading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
          <Box textAlign="center">
            <CircularProgress size={60} />
            <Typography variant="h6" sx={{ mt: 2 }}>
              Loading your tasks...
            </Typography>
          </Box>
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error">
          <Typography variant="h6" gutterBottom>
            Failed to load tasks
          </Typography>
          <Typography variant="body2">
            {error.response?.data?.message || error.message}
          </Typography>
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        My Tasks
      </Typography>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        User ID: {user?.id || 'Not available'} | Tasks count: {tasks?.length || 0}
      </Typography>

      <Paper sx={{ mb: 3 }}>
        <Tabs value={tabValue} onChange={handleTabChange}>
          <Tab label="Pending" />
          <Tab label="In Progress" />
          <Tab label="Completed" />
          <Tab label="Blocked" />
        </Tabs>
      </Paper>

      <Grid container spacing={3}>
        {filteredTasks.map((task) => (
          <Grid item xs={12} md={6} key={task.id}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" mb={2}>
                  {getStatusIcon(task.status)}
                  <Typography variant="h6" component="div" sx={{ ml: 1 }}>
                    {task.title || task.stepName}
                  </Typography>
                  <Chip
                    label={task.status}
                    size="small"
                    color={getStatusColor(task.status)}
                    sx={{ ml: 'auto' }}
                  />
                </Box>
                
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  {task.description}
                </Typography>
                
                <Box display="flex" justifyContent="space-between" mb={2}>
                  <Typography variant="body2">
                    Priority: {task.priority}
                  </Typography>
                  <Typography variant="body2">
                    Due: {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'N/A'}
                  </Typography>
                </Box>

                {task.workflowInstance && (
                  <Typography variant="body2" color="text.secondary">
                    Workflow: {task.workflowInstance.workflow?.name}
                  </Typography>
                )}
              </CardContent>
              
              <Box sx={{ p: 2, pt: 0 }}>
                {task.status === 'READY'  && (
                  <Button
                    size="small"
                    variant="contained"
                    onClick={() => handleTaskAction(task, 'start')}
                    sx={{ mr: 1 }}
                  >
                    Start
                  </Button>
                )}
                {task.status === 'IN_PROGRESS' && (
                  <>
                    <Button
                      size="small"
                      variant="contained"
                      color="success"
                      onClick={() => handleTaskAction(task, 'complete')}
                      sx={{ mr: 1 }}
                    >
                      Complete
                    </Button>
                    <Button
                      size="small"
                      variant="outlined"
                      color="error"
                      onClick={() => handleTaskAction(task, 'reject')}
                    >
                      Reject
                    </Button>
                  </>
                )}
              </Box>
            </Card>
          </Grid>
        ))}
      </Grid>

      {filteredTasks.length === 0 && (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary">
            No tasks found for this category
          </Typography>
        </Paper>
      )}

      {/* Task Action Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {action === 'complete' ? 'Complete Task' : 
           action === 'reject' ? 'Reject Task' : 'Start Task'}
        </DialogTitle>
        <DialogContent>
          {selectedTask && (
            <Box>
              <Typography variant="h6" gutterBottom>
                {selectedTask.title || selectedTask.stepName}
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                {selectedTask.description}
              </Typography>
              
              {(action === 'complete' || action === 'reject') && (
                <TextField
                  fullWidth
                  label="Comment (Optional)"
                  multiline
                  rows={3}
                  sx={{ mt: 2 }}
                  placeholder="Add any comments about this task..."
                />
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button 
            variant="contained"
            onClick={() => handleSubmitAction()}
            disabled={updateTaskMutation.isLoading}
          >
            {action === 'complete' ? 'Complete' : 
             action === 'reject' ? 'Reject' : 'Start'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Tasks;
