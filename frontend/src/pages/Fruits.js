import React, { useState } from 'react';
import {
  Container,
  Typography,
  Button,
  Box,
  Paper,
  Grid,
  Card,
  CardContent,
  CardActions,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  ShoppingCart as FruitIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import axios from 'axios';
import toast from 'react-hot-toast';

const Fruits = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [editingFruit, setEditingFruit] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    price: '',
    quantity: '',
    description: '',
    category: '',
  });

  const queryClient = useQueryClient();

  const { data: fruits, isLoading } = useQuery(
    'fruits',
    () => axios.get('/api/fruits').then(res => res.data)
  );

  const createMutation = useMutation(
    (fruitData) => axios.post('/api/fruits', fruitData),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('fruits');
        toast.success('Fruit created successfully!');
        handleCloseDialog();
      },
      onError: (error) => {
        toast.error('Failed to create fruit: ' + error.response?.data?.message);
      },
    }
  );

  const updateMutation = useMutation(
    ({ id, ...fruitData }) => axios.put(`/api/fruits/${id}`, fruitData),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('fruits');
        toast.success('Fruit updated successfully!');
        handleCloseDialog();
      },
      onError: (error) => {
        toast.error('Failed to update fruit: ' + error.response?.data?.message);
      },
    }
  );

  const deleteMutation = useMutation(
    (id) => axios.delete(`/api/fruits/${id}`),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('fruits');
        toast.success('Fruit deleted successfully!');
      },
      onError: (error) => {
        toast.error('Failed to delete fruit: ' + error.response?.data?.message);
      },
    }
  );

  const handleOpenDialog = (fruit = null) => {
    setEditingFruit(fruit);
    if (fruit) {
      setFormData({
        name: fruit.name || '',
        price: fruit.price || '',
        quantity: fruit.quantity || '',
        description: fruit.description || '',
        category: fruit.category || '',
      });
    } else {
      setFormData({
        name: '',
        price: '',
        quantity: '',
        description: '',
        category: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingFruit(null);
    setFormData({
      name: '',
      price: '',
      quantity: '',
      description: '',
      category: '',
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const fruitData = {
      ...formData,
      price: parseFloat(formData.price),
      quantity: parseInt(formData.quantity),
    };

    if (editingFruit) {
      updateMutation.mutate({ id: editingFruit.id, ...fruitData });
    } else {
      createMutation.mutate(fruitData);
    }
  };

  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to delete this fruit?')) {
      deleteMutation.mutate(id);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      'DRAFT': 'default',
      'PENDING_APPROVAL': 'warning',
      'APPROVED': 'success',
      'REJECTED': 'error',
      'ACTIVE': 'info',
    };
    return colors[status] || 'default';
  };

  if (isLoading) {
    return (
      <Container>
        <Typography>Loading fruits...</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" gutterBottom>
          Fruits Management
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Add Fruit
        </Button>
      </Box>

      <Grid container spacing={3}>
        {fruits?.map((fruit) => (
          <Grid item xs={12} sm={6} md={4} key={fruit.id}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" mb={2}>
                  <FruitIcon sx={{ mr: 1 }} />
                  <Typography variant="h6" component="div">
                    {fruit.name}
                  </Typography>
                </Box>
                
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  {fruit.description}
                </Typography>
                
                <Box display="flex" justifyContent="space-between" mb={1}>
                  <Typography variant="body2">
                    Price: ${fruit.price}
                  </Typography>
                  <Typography variant="body2">
                    Qty: {fruit.quantity}
                  </Typography>
                </Box>
                
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Chip
                    label={fruit.category}
                    size="small"
                    color="primary"
                    variant="outlined"
                  />
                  <Chip
                    label={fruit.status || 'DRAFT'}
                    size="small"
                    color={getStatusColor(fruit.status)}
                  />
                </Box>
              </CardContent>
              
              <CardActions>
                <Tooltip title="View Details">
                  <IconButton size="small">
                    <ViewIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Edit">
                  <IconButton 
                    size="small" 
                    onClick={() => handleOpenDialog(fruit)}
                  >
                    <EditIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Delete">
                  <IconButton 
                    size="small" 
                    onClick={() => handleDelete(fruit.id)}
                    color="error"
                  >
                    <DeleteIcon />
                  </IconButton>
                </Tooltip>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Add/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingFruit ? 'Edit Fruit' : 'Add New Fruit'}
        </DialogTitle>
        <form onSubmit={handleSubmit}>
          <DialogContent>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Fruit Name"
                  name="name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Price"
                  name="price"
                  type="number"
                  step="0.01"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Quantity"
                  name="quantity"
                  type="number"
                  value={formData.quantity}
                  onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
                  required
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Description"
                  name="description"
                  multiline
                  rows={3}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth>
                  <InputLabel>Category</InputLabel>
                  <Select
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    label="Category"
                  >
                    <MenuItem value="Fruit">Fruit</MenuItem>
                    <MenuItem value="Berry">Berry</MenuItem>
                    <MenuItem value="Citrus">Citrus</MenuItem>
                    <MenuItem value="Tropical">Tropical</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancel</Button>
            <Button 
              type="submit" 
              variant="contained"
              disabled={createMutation.isLoading || updateMutation.isLoading}
            >
              {editingFruit ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Container>
  );
};

export default Fruits;
