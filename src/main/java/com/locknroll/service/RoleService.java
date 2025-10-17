package com.locknroll.service;

import com.locknroll.dto.RoleDto;
import com.locknroll.entity.Permission;
import com.locknroll.entity.Role;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.RoleAlreadyExistsException;
import com.locknroll.repository.PermissionRepository;
import com.locknroll.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for Role management operations
 */
@Service
@Transactional
public class RoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    /**
     * Create a new role
     */
    @CacheEvict(value = {"roles", "activeRoles"}, allEntries = true)
    public RoleDto createRole(RoleDto roleDto, String createdBy) {
        logger.info("Creating new role: {}", roleDto.getName());
        
        // Check if role name already exists
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new RoleAlreadyExistsException("Role already exists: " + roleDto.getName());
        }
        
        Role role = new Role();
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setIsActive(roleDto.getIsActive() != null ? roleDto.getIsActive() : true);
        role.setCreatedBy(createdBy);
        
        // Assign permissions if provided
        if (roleDto.getPermissions() != null && !roleDto.getPermissions().isEmpty()) {
            Set<Permission> permissions = roleDto.getPermissions().stream()
                    .map(permissionName -> permissionRepository.findByName(permissionName)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        Role savedRole = roleRepository.save(role);
        logger.info("Role created successfully: {}", savedRole.getName());
        
        return convertToDto(savedRole);
    }
    
    /**
     * Get role by ID
     */
    @Cacheable(value = "roles", key = "#id")
    public RoleDto getRoleById(Long id) {
        logger.debug("Fetching role by ID: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        return convertToDto(role);
    }
    
    /**
     * Get role by name
     */
    @Cacheable(value = "roles", key = "#name")
    public RoleDto getRoleByName(String name) {
        logger.debug("Fetching role by name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return convertToDto(role);
    }
    
    /**
     * Get all roles with pagination
     */
    public Page<RoleDto> getAllRoles(Pageable pageable) {
        logger.debug("Fetching all roles with pagination");
        Page<Role> roles = roleRepository.findAll(pageable);
        return roles.map(this::convertToDto);
    }
    
    /**
     * Get active roles
     */
    @Cacheable(value = "activeRoles")
    public List<RoleDto> getActiveRoles() {
        logger.debug("Fetching all active roles");
        List<Role> roles = roleRepository.findByIsActiveTrue();
        return roles.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Get roles by permission
     */
    @Cacheable(value = "rolesByPermission", key = "#permissionName")
    public List<RoleDto> getRolesByPermission(String permissionName) {
        logger.debug("Fetching roles by permission: {}", permissionName);
        List<Role> roles = roleRepository.findByPermissionName(permissionName);
        return roles.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Update role
     */
    @CacheEvict(value = {"roles", "activeRoles", "rolesByPermission"}, allEntries = true)
    public RoleDto updateRole(Long id, RoleDto roleDto, String updatedBy) {
        logger.info("Updating role: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        
        // Check if role name is being changed and if it already exists
        if (!role.getName().equals(roleDto.getName()) && 
            roleRepository.existsByName(roleDto.getName())) {
            throw new RoleAlreadyExistsException("Role already exists: " + roleDto.getName());
        }
        
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setIsActive(roleDto.getIsActive());
        role.setUpdatedBy(updatedBy);
        
        // Update permissions if provided
        if (roleDto.getPermissions() != null) {
            Set<Permission> permissions = roleDto.getPermissions().stream()
                    .map(permissionName -> permissionRepository.findByName(permissionName)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        Role savedRole = roleRepository.save(role);
        logger.info("Role updated successfully: {}", savedRole.getName());
        
        return convertToDto(savedRole);
    }
    
    /**
     * Deactivate role (soft delete)
     */
    @CacheEvict(value = {"roles", "activeRoles", "rolesByPermission"}, allEntries = true)
    public void deactivateRole(Long id, String updatedBy) {
        logger.info("Deactivating role: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        
        role.setIsActive(false);
        role.setUpdatedBy(updatedBy);
        roleRepository.save(role);
        
        logger.info("Role deactivated successfully: {}", role.getName());
    }
    
    /**
     * Activate role
     */
    @CacheEvict(value = {"roles", "activeRoles", "rolesByPermission"}, allEntries = true)
    public void activateRole(Long id, String updatedBy) {
        logger.info("Activating role: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        
        role.setIsActive(true);
        role.setUpdatedBy(updatedBy);
        roleRepository.save(role);
        
        logger.info("Role activated successfully: {}", role.getName());
    }
    
    /**
     * Assign permission to role
     */
    @CacheEvict(value = {"roles", "activeRoles", "rolesByPermission"}, allEntries = true)
    public RoleDto assignPermissionToRole(Long roleId, String permissionName, String updatedBy) {
        logger.info("Assigning permission {} to role {}", permissionName, roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName));
        
        role.addPermission(permission);
        role.setUpdatedBy(updatedBy);
        
        Role savedRole = roleRepository.save(role);
        logger.info("Permission assigned successfully");
        
        return convertToDto(savedRole);
    }
    
    /**
     * Remove permission from role
     */
    @CacheEvict(value = {"roles", "activeRoles", "rolesByPermission"}, allEntries = true)
    public RoleDto removePermissionFromRole(Long roleId, String permissionName, String updatedBy) {
        logger.info("Removing permission {} from role {}", permissionName, roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName));
        
        role.removePermission(permission);
        role.setUpdatedBy(updatedBy);
        
        Role savedRole = roleRepository.save(role);
        logger.info("Permission removed successfully");
        
        return convertToDto(savedRole);
    }
    
    /**
     * Get roles with workflow steps assigned
     */
    public List<RoleDto> getRolesWithWorkflowSteps() {
        logger.debug("Fetching roles with workflow steps");
        List<Role> roles = roleRepository.findRolesWithWorkflowSteps();
        return roles.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Get unassigned roles
     */
    public List<RoleDto> getUnassignedRoles() {
        logger.debug("Fetching unassigned roles");
        List<Role> roles = roleRepository.findUnassignedRoles();
        return roles.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Check if role has specific permission
     */
    public boolean roleHasPermission(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
        return role.hasPermission(permissionName);
    }
    
    /**
     * Convert Role entity to RoleDto
     */
    private RoleDto convertToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setIsActive(role.getIsActive());
        // dto.setCreatedAt(role.getCreatedAt());
        // dto.setUpdatedAt(role.getUpdatedAt());
        dto.setCreatedBy(role.getCreatedBy());
        dto.setUpdatedBy(role.getUpdatedBy());
        
        // Set permission names
        if (role.getPermissions() != null) {
            Set<String> permissionNames = role.getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());
            dto.setPermissions(permissionNames);
        }
        
        // Set user count
        dto.setUserCount(roleRepository.countActiveUsersByRoleId(role.getId()));
        
        return dto;
    }
}
