import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  AccountTree as WorkflowIcon,
  PlayArrow as StartIcon,
  Stop as StopIcon,
  CheckCircle as CompletedIcon,
  Pending as PendingIcon,
  ExpandMore as ExpandMoreIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import axios from 'axios';
import toast from 'react-hot-toast';

const Workflows = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);
  const [formData, setFormData] = useState({
    entityType: '',
    entityId: '',
    workflowName: '',
  });

  const queryClient = useQueryClient();

  const { data: workflows, isLoading: workflowsLoading } = useQuery(
    'workflows',
    () => axios.get('/api/workflows').then(res => res.data)
  );

  const { data: workflowInstances, isLoading: instancesLoading } = useQuery(
    'workflow-instances',
    () => axios.get('/api/workflow-instances').then(res => res.data),
    {
      enabled: false, // Only load when needed
    }
  );

  const startWorkflowMutation = useMutation(
    (workflowData) => axios.post('/api/workflow-execution/start', workflowData),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('workflow-instances');
        toast.success('Workflow started successfully!');
        setOpenDialog(false);
      },
      onError: (error) => {
        toast.error('Failed to start workflow: ' + error.response?.data?.message);
      },
    }
  );

  const handleStartWorkflow = (workflow) => {
    setSelectedWorkflow(workflow);
    setFormData({
      entityType: 'FRUIT',
      entityId: '',
      workflowName: workflow.name,
    });
    setOpenDialog(true);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    startWorkflowMutation.mutate({
      ...formData,
      startedBy: 'current-user', // This would come from auth context
    });
  };

  const getStatusColor = (status) => {
    const colors = {
      'PENDING': 'default',
      'IN_PROGRESS': 'primary',
      'COMPLETED': 'success',
      'REJECTED': 'error',
      'CANCELLED': 'warning',
    };
    return colors[status] || 'default';
  };

  const getStatusIcon = (status) => {
    const icons = {
      'PENDING': <PendingIcon />,
      'IN_PROGRESS': <StartIcon />,
      'COMPLETED': <CompletedIcon />,
      'REJECTED': <StopIcon />,
      'CANCELLED': <StopIcon />,
    };
    return icons[status] || <WorkflowIcon />;
  };

  if (workflowsLoading) {
    return (
      <Container>
        <Typography>Loading workflows...</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" gutterBottom>
          Workflow Management
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setOpenDialog(true)}
        >
          Start Workflow
        </Button>
      </Box>

      <Grid container spacing={3}>
        {workflows?.map((workflow) => (
          <Grid item xs={12} md={6} key={workflow.id}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" mb={2}>
                  <WorkflowIcon sx={{ mr: 1 }} />
                  <Typography variant="h6" component="div">
                    {workflow.name}
                  </Typography>
                  <Chip
                    label={workflow.isActive ? 'Active' : 'Inactive'}
                    size="small"
                    color={workflow.isActive ? 'success' : 'default'}
                    sx={{ ml: 'auto' }}
                  />
                </Box>
                
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  {workflow.description}
                </Typography>
                
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Entity Type: {workflow.entityType}
                </Typography>

                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="subtitle2">
                      Workflow Steps ({workflow.steps?.length || 0})
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <List dense>
                      {workflow.steps?.map((step, index) => (
                        <ListItem key={step.id}>
                          <ListItemIcon>
                            <Typography variant="body2" color="text.secondary">
                              {step.stepOrder}
                            </Typography>
                          </ListItemIcon>
                          <ListItemText
                            primary={step.stepName}
                            secondary={`${step.stepType} - ${step.assignedRoleName}`}
                          />
                          <Chip
                            label={step.isRequired ? 'Required' : 'Optional'}
                            size="small"
                            color={step.isRequired ? 'primary' : 'default'}
                          />
                        </ListItem>
                      ))}
                    </List>
                  </AccordionDetails>
                </Accordion>
              </CardContent>
              
              <CardActions>
                <Button
                  size="small"
                  variant="contained"
                  startIcon={<StartIcon />}
                  onClick={() => handleStartWorkflow(workflow)}
                  disabled={!workflow.isActive}
                >
                  Start Workflow
                </Button>
                <Button size="small" variant="outlined">
                  View Details
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Start Workflow Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          Start Workflow: {selectedWorkflow?.name}
        </DialogTitle>
        <form onSubmit={handleSubmit}>
          <DialogContent>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <FormControl fullWidth>
                  <InputLabel>Entity Type</InputLabel>
                  <Select
                    value={formData.entityType}
                    onChange={(e) => setFormData({ ...formData, entityType: e.target.value })}
                    label="Entity Type"
                  >
                    <MenuItem value="FRUIT">Fruit</MenuItem>
                    <MenuItem value="SELLER">Seller</MenuItem>
                    <MenuItem value="ORDER">Order</MenuItem>
                    <MenuItem value="GENERIC">Generic</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Entity ID"
                  name="entityId"
                  value={formData.entityId}
                  onChange={(e) => setFormData({ ...formData, entityId: e.target.value })}
                  required
                  helperText="Enter the ID of the entity to process"
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
            <Button 
              type="submit" 
              variant="contained"
              disabled={startWorkflowMutation.isLoading}
            >
              Start Workflow
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Container>
  );
};

export default Workflows;
