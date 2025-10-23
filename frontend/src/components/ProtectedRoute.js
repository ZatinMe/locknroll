import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { CircularProgress, Box } from '@mui/material';

const ProtectedRoute = ({ children, requiredRoles = [] }) => {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="50vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Check role-based access
  if (requiredRoles.length > 0) {
    const hasRequiredRole = user?.roles?.some(role => 
      requiredRoles.includes(role) || requiredRoles.includes(role.replace('_ROLE', ''))
    );
    
    if (!hasRequiredRole) {
      return (
        <Box p={3}>
          <h2>Access Denied</h2>
          <p>You don't have permission to access this page.</p>
        </Box>
      );
    }
  }

  return children;
};

export default ProtectedRoute;
