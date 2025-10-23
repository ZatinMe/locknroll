import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Menu,
  MenuItem,
  Box,
  Avatar,
  Chip,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  useMediaQuery,
  useTheme,
  Badge,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  ShoppingCart as FruitsIcon,
  Assignment as TasksIcon,
  AccountTree as WorkflowsIcon,
  People as UsersIcon,
  AccountCircle,
  ExitToApp,
  Menu as MenuIcon,
  Notifications as NotificationsIcon,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [anchorEl, setAnchorEl] = useState(null);
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleMenu = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
    handleClose();
  };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const isActiveRoute = (path) => {
    return location.pathname === path;
  };

  const getRoleColor = (role) => {
    const roleColors = {
      'ADMIN': 'error',
      'BACKOFFICE': 'primary',
      'MANAGER': 'secondary',
      'FINANCE': 'success',
      'QUALITY': 'warning',
      'SELLER': 'info',
    };
    return roleColors[role] || 'default';
  };

  const navigationItems = [
    { label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon />, roles: ['ADMIN', 'BACKOFFICE', 'MANAGER', 'FINANCE', 'QUALITY', 'SELLER'] },
    { label: 'Fruits', path: '/fruits', icon: <FruitsIcon />, roles: ['SELLER', 'ADMIN', 'BACKOFFICE'] },
    { label: 'Tasks', path: '/tasks', icon: <TasksIcon />, roles: ['ADMIN', 'BACKOFFICE', 'MANAGER', 'FINANCE', 'QUALITY'] },
    { label: 'Workflows', path: '/workflows', icon: <WorkflowsIcon />, roles: ['ADMIN', 'BACKOFFICE', 'MANAGER'] },
    { label: 'Users', path: '/users', icon: <UsersIcon />, roles: ['ADMIN', 'BACKOFFICE'] },
  ];

  const canAccess = (item) => {
    if (item.roles.length === 0) return true;
    return user?.roles?.some(role => 
      item.roles.includes(role) || item.roles.includes(role.replace('_ROLE', ''))
    );
  };

  const drawer = (
    <Box>
      <Toolbar>
        <Typography variant="h6" noWrap component="div">
          LockNRoll
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {navigationItems
          .filter(canAccess)
          .map((item) => (
            <ListItem
              button
              key={item.path}
              component={Link}
              to={item.path}
              selected={isActiveRoute(item.path)}
              onClick={() => setMobileOpen(false)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItem>
          ))}
      </List>
      <Divider />
      <List>
        <ListItem button onClick={handleLogout}>
          <ListItemIcon>
            <ExitToApp />
          </ListItemIcon>
          <ListItemText primary="Logout" />
        </ListItem>
      </List>
    </Box>
  );

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          {isMobile && (
            <IconButton
              color="inherit"
              aria-label="open drawer"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{ mr: 2 }}
            >
              <MenuIcon />
            </IconButton>
          )}
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            LockNRoll
          </Typography>

        {isAuthenticated ? (
          <>
            {!isMobile && (
              <Box sx={{ display: 'flex', gap: 1, mr: 2 }}>
                {navigationItems
                  .filter(canAccess)
                  .map((item) => (
                    <Button
                      key={item.path}
                      color="inherit"
                      component={Link}
                      to={item.path}
                      startIcon={item.icon}
                      variant={isActiveRoute(item.path) ? 'contained' : 'text'}
                    >
                      {item.label}
                    </Button>
                  ))}
              </Box>
            )}

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {user?.roles?.map((role) => (
                <Chip
                  key={role}
                  label={role.replace('_ROLE', '')}
                  size="small"
                  color={getRoleColor(role.replace('_ROLE', ''))}
                  variant="outlined"
                />
              ))}
              
              <IconButton
                size="large"
                aria-label="account of current user"
                aria-controls="menu-appbar"
                aria-haspopup="true"
                onClick={handleMenu}
                color="inherit"
              >
                <Avatar sx={{ width: 32, height: 32 }}>
                  {user?.fullName?.charAt(0)}
                </Avatar>
              </IconButton>
              
              <Menu
                id="menu-appbar"
                anchorEl={anchorEl}
                anchorOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                keepMounted
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={Boolean(anchorEl)}
                onClose={handleClose}
              >
                <MenuItem disabled>
                  <Typography variant="body2" color="text.secondary">
                    {user?.fullName}
                  </Typography>
                </MenuItem>
                <MenuItem onClick={handleLogout}>
                  <ExitToApp sx={{ mr: 1 }} />
                  Logout
                </MenuItem>
              </Menu>
            </Box>
          </>
        ) : (
          <Button color="inherit" component={Link} to="/login">
            Login
          </Button>
        )}
      </Toolbar>
    </AppBar>
    
    <Drawer
      variant="temporary"
      anchor="left"
      open={mobileOpen}
      onClose={handleDrawerToggle}
      ModalProps={{
        keepMounted: true, // Better open performance on mobile.
      }}
      sx={{
        display: { xs: 'block', md: 'none' },
        '& .MuiDrawer-paper': { boxSizing: 'border-box', width: 240 },
      }}
    >
      {drawer}
    </Drawer>
    </>
  );
};

export default Navbar;
