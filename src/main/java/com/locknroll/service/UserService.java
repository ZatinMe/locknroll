package com.locknroll.service;

import com.locknroll.dto.UserDto;
import com.locknroll.entity.Role;
import com.locknroll.entity.User;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.UserAlreadyExistsException;
import com.locknroll.repository.RoleRepository;
import com.locknroll.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for User management operations
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Create a new user
     */
    @CacheEvict(value = "users", allEntries = true)
    public UserDto createUser(UserDto userDto, String createdBy) {
        logger.info("Creating new user: {}", userDto.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + userDto.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + userDto.getEmail());
        }
        
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode("defaultPassword123")); // Default password
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setIsActive(userDto.getIsActive() != null ? userDto.getIsActive() : true);
        user.setIsEmailVerified(false);
        user.setCreatedBy(createdBy);
        
        // Assign roles if provided
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> roles = userDto.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", savedUser.getUsername());
        
        return convertToDto(savedUser);
    }
    
    /**
     * Get user by ID
     */
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return convertToDto(user);
    }
    
    /**
     * Get user by username
     */
    @Cacheable(value = "users", key = "#username")
    public UserDto getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDto(user);
    }
    
    /**
     * Get all users with pagination
     */
    public Page<UserDto> getAllUsers(Pageable pageable) {
        logger.debug("Fetching all users with pagination");
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }
    
    /**
     * Get active users
     */
    @Cacheable(value = "activeUsers")
    public List<UserDto> getActiveUsers() {
        logger.debug("Fetching all active users");
        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Get users by role
     */
    @Cacheable(value = "usersByRole", key = "#roleName")
    public List<UserDto> getUsersByRole(String roleName) {
        logger.debug("Fetching users by role: {}", roleName);
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Update user
     */
    @CacheEvict(value = {"users", "activeUsers", "usersByRole"}, allEntries = true)
    public UserDto updateUser(Long id, UserDto userDto, String updatedBy) {
        logger.info("Updating user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Check if username is being changed and if it already exists
        if (!user.getUsername().equals(userDto.getUsername()) && 
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + userDto.getUsername());
        }
        
        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(userDto.getEmail()) && 
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + userDto.getEmail());
        }
        
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setIsActive(userDto.getIsActive());
        user.setIsEmailVerified(userDto.getIsEmailVerified());
        user.setUpdatedBy(updatedBy);
        
        // Update roles if provided
        if (userDto.getRoles() != null) {
            Set<Role> roles = userDto.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        User savedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", savedUser.getUsername());
        
        return convertToDto(savedUser);
    }
    
    /**
     * Deactivate user (soft delete)
     */
    @CacheEvict(value = {"users", "activeUsers", "usersByRole"}, allEntries = true)
    public void deactivateUser(Long id, String updatedBy) {
        logger.info("Deactivating user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.setIsActive(false);
        user.setUpdatedBy(updatedBy);
        userRepository.save(user);
        
        logger.info("User deactivated successfully: {}", user.getUsername());
    }
    
    /**
     * Activate user
     */
    @CacheEvict(value = {"users", "activeUsers", "usersByRole"}, allEntries = true)
    public void activateUser(Long id, String updatedBy) {
        logger.info("Activating user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.setIsActive(true);
        user.setUpdatedBy(updatedBy);
        userRepository.save(user);
        
        logger.info("User activated successfully: {}", user.getUsername());
    }
    
    /**
     * Assign role to user
     */
    @CacheEvict(value = {"users", "activeUsers", "usersByRole"}, allEntries = true)
    public UserDto assignRoleToUser(Long userId, String roleName, String updatedBy) {
        logger.info("Assigning role {} to user {}", roleName, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        user.addRole(role);
        user.setUpdatedBy(updatedBy);
        
        User savedUser = userRepository.save(user);
        logger.info("Role assigned successfully");
        
        return convertToDto(savedUser);
    }
    
    /**
     * Remove role from user
     */
    @CacheEvict(value = {"users", "activeUsers", "usersByRole"}, allEntries = true)
    public UserDto removeRoleFromUser(Long userId, String roleName, String updatedBy) {
        logger.info("Removing role {} from user {}", roleName, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        user.removeRole(role);
        user.setUpdatedBy(updatedBy);
        
        User savedUser = userRepository.save(user);
        logger.info("Role removed successfully");
        
        return convertToDto(savedUser);
    }
    
    /**
     * Update user's last login
     */
    @CacheEvict(value = "users", key = "#username")
    public void updateLastLogin(String username) {
        logger.debug("Updating last login for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Check if user has specific role
     */
    public boolean userHasRole(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return user.hasRole(roleName);
    }
    
    /**
     * Get users with pending tasks
     */
    public List<UserDto> getUsersWithPendingTasks() {
        logger.debug("Fetching users with pending tasks");
        List<User> users = userRepository.findUsersWithPendingTasks();
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Get users with overdue tasks
     */
    public List<UserDto> getUsersWithOverdueTasks() {
        logger.debug("Fetching users with overdue tasks");
        List<User> users = userRepository.findUsersWithOverdueTasks();
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Convert User entity to UserDto
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        dto.setIsEmailVerified(user.getIsEmailVerified());
        // dto.setLastLogin(user.getLastLogin());
        // dto.setCreatedAt(user.getCreatedAt());
        // dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());
        
        // Set role names
        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        }
        
        return dto;
    }
}
