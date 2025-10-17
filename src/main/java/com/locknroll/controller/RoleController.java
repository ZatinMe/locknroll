package com.locknroll.controller;

import com.locknroll.dto.RoleDto;
import com.locknroll.service.RoleService;
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
 * REST Controller for Role management
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
    @Autowired
    private RoleService roleService;
    
    /**
     * Create a new role
     */
    @PostMapping
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        logger.info("Creating new role: {}", roleDto.getName());
        try {
            RoleDto createdRole = roleService.createRole(roleDto, "system"); // TODO: Get from security context
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (Exception e) {
            logger.error("Error creating role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        logger.debug("Fetching role by ID: {}", id);
        try {
            RoleDto role = roleService.getRoleById(id);
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            logger.error("Error fetching role: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get role by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<RoleDto> getRoleByName(@PathVariable String name) {
        logger.debug("Fetching role by name: {}", name);
        try {
            RoleDto role = roleService.getRoleByName(name);
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            logger.error("Error fetching role: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all roles with pagination
     */
    @GetMapping
    public ResponseEntity<Page<RoleDto>> getAllRoles(Pageable pageable) {
        logger.debug("Fetching all roles with pagination");
        try {
            Page<RoleDto> roles = roleService.getAllRoles(pageable);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Error fetching roles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get active roles
     */
    @GetMapping("/active")
    public ResponseEntity<List<RoleDto>> getActiveRoles() {
        logger.debug("Fetching active roles");
        try {
            List<RoleDto> roles = roleService.getActiveRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Error fetching active roles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get roles by permission
     */
    @GetMapping("/permission/{permissionName}")
    public ResponseEntity<List<RoleDto>> getRolesByPermission(@PathVariable String permissionName) {
        logger.debug("Fetching roles by permission: {}", permissionName);
        try {
            List<RoleDto> roles = roleService.getRolesByPermission(permissionName);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Error fetching roles by permission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update role
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDto roleDto) {
        logger.info("Updating role: {}", id);
        try {
            RoleDto updatedRole = roleService.updateRole(id, roleDto, "system"); // TODO: Get from security context
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            logger.error("Error updating role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Deactivate role
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateRole(@PathVariable Long id) {
        logger.info("Deactivating role: {}", id);
        try {
            roleService.deactivateRole(id, "system"); // TODO: Get from security context
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deactivating role: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Activate role
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateRole(@PathVariable Long id) {
        logger.info("Activating role: {}", id);
        try {
            roleService.activateRole(id, "system"); // TODO: Get from security context
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error activating role: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Assign permission to role
     */
    @PostMapping("/{id}/permissions/{permissionName}")
    public ResponseEntity<RoleDto> assignPermissionToRole(@PathVariable Long id, @PathVariable String permissionName) {
        logger.info("Assigning permission {} to role {}", permissionName, id);
        try {
            RoleDto updatedRole = roleService.assignPermissionToRole(id, permissionName, "system"); // TODO: Get from security context
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            logger.error("Error assigning permission to role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Remove permission from role
     */
    @DeleteMapping("/{id}/permissions/{permissionName}")
    public ResponseEntity<RoleDto> removePermissionFromRole(@PathVariable Long id, @PathVariable String permissionName) {
        logger.info("Removing permission {} from role {}", permissionName, id);
        try {
            RoleDto updatedRole = roleService.removePermissionFromRole(id, permissionName, "system"); // TODO: Get from security context
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            logger.error("Error removing permission from role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Get roles with workflow steps assigned
     */
    @GetMapping("/workflow-steps")
    public ResponseEntity<List<RoleDto>> getRolesWithWorkflowSteps() {
        logger.debug("Fetching roles with workflow steps");
        try {
            List<RoleDto> roles = roleService.getRolesWithWorkflowSteps();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Error fetching roles with workflow steps: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get unassigned roles
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<RoleDto>> getUnassignedRoles() {
        logger.debug("Fetching unassigned roles");
        try {
            List<RoleDto> roles = roleService.getUnassignedRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Error fetching unassigned roles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Check if role has specific permission
     */
    @GetMapping("/{roleName}/has-permission/{permissionName}")
    public ResponseEntity<Boolean> roleHasPermission(@PathVariable String roleName, @PathVariable String permissionName) {
        logger.debug("Checking if role {} has permission {}", roleName, permissionName);
        try {
            boolean hasPermission = roleService.roleHasPermission(roleName, permissionName);
            return ResponseEntity.ok(hasPermission);
        } catch (Exception e) {
            logger.error("Error checking role permission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
