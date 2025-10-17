package com.locknroll.controller;

import com.locknroll.dto.UserDto;
import com.locknroll.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User management
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new user
     */
    /**
     * Example curl command:
      curl -X POST http://localhost:8080/api/users \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer YOUR_JWT_TOKEN" \
        -d '{
          "username": "johndoe",
          "email": "john.doe@example.com",
          "firstName": "John", 
          "lastName": "Doe",
          "phoneNumber": "1234567890",
          "isActive": true,
          "isEmailVerified": false,
          "roles": ["USER_ROLE_AS_REQD"]
        }'
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        logger.info("Creating new user: {}", userDto.getUsername());
        try {
            UserDto createdUser = userService.createUser(userDto, "system"); // TODO: Get from security context
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        logger.debug("Fetching user by ID: {}", id);
        try {
            UserDto user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        logger.debug("Fetching user by username: {}", username);
        try {
            UserDto user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all users with pagination
     */
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        logger.debug("Fetching all users with pagination");
        try {
            Page<UserDto> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get active users
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        logger.debug("Fetching active users");
        try {
            List<UserDto> users = userService.getActiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching active users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get users by role
     */
    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String roleName) {
        logger.debug("Fetching users by role: {}", roleName);
        try {
            List<UserDto> users = userService.getUsersByRole(roleName);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users by role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        logger.info("Updating user: {}", id);
        try {
            UserDto updatedUser = userService.updateUser(id, userDto, "system"); // TODO: Get from security context
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Deactivate user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        logger.info("Deactivating user: {}", id);
        try {
            userService.deactivateUser(id, "system"); // TODO: Get from security context
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deactivating user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Activate user
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        logger.info("Activating user: {}", id);
        try {
            userService.activateUser(id, "system"); // TODO: Get from security context
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error activating user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Assign role to user
     */
    @PostMapping("/{id}/roles/{roleName}")
    public ResponseEntity<UserDto> assignRoleToUser(@PathVariable Long id, @PathVariable String roleName) {
        logger.info("Assigning role {} to user {}", roleName, id);
        try {
            UserDto updatedUser = userService.assignRoleToUser(id, roleName, "system"); // TODO: Get from security context
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error assigning role to user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Remove role from user
     */
    @DeleteMapping("/{id}/roles/{roleName}")
    public ResponseEntity<UserDto> removeRoleFromUser(@PathVariable Long id, @PathVariable String roleName) {
        logger.info("Removing role {} from user {}", roleName, id);
        try {
            UserDto updatedUser = userService.removeRoleFromUser(id, roleName, "system"); // TODO: Get from security context
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error removing role from user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Get users with pending tasks
     */
    @GetMapping("/pending-tasks")
    public ResponseEntity<List<UserDto>> getUsersWithPendingTasks() {
        logger.debug("Fetching users with pending tasks");
        try {
            List<UserDto> users = userService.getUsersWithPendingTasks();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users with pending tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get users with overdue tasks
     */
    @GetMapping("/overdue-tasks")
    public ResponseEntity<List<UserDto>> getUsersWithOverdueTasks() {
        logger.debug("Fetching users with overdue tasks");
        try {
            List<UserDto> users = userService.getUsersWithOverdueTasks();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users with overdue tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Check if user has specific role
     */
    @GetMapping("/{username}/has-role/{roleName}")
    public ResponseEntity<Boolean> userHasRole(@PathVariable String username, @PathVariable String roleName) {
        logger.debug("Checking if user {} has role {}", username, roleName);
        try {
            boolean hasRole = userService.userHasRole(username, roleName);
            return ResponseEntity.ok(hasRole);
        } catch (Exception e) {
            logger.error("Error checking user role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
