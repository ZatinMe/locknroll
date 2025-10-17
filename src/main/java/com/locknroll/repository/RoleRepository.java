package com.locknroll.repository;

import com.locknroll.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Role entity operations
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if role name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find active roles
     */
    List<Role> findByIsActiveTrue();
    
    /**
     * Find roles by permission name
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName AND r.isActive = true")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);
    
    /**
     * Find roles by multiple permission names
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name IN :permissionNames AND r.isActive = true")
    List<Role> findByPermissionNames(@Param("permissionNames") List<String> permissionNames);
    
    /**
     * Find roles with specific permission resource and action
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.resource = :resource AND p.action = :action AND r.isActive = true")
    List<Role> findByPermissionResourceAndAction(@Param("resource") String resource, @Param("action") String action);
    
    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId AND u.isActive = true")
    Long countActiveUsersByRoleId(@Param("roleId") Long roleId);
    
    /**
     * Find roles with no users assigned
     */
    @Query("SELECT r FROM Role r LEFT JOIN r.users u WHERE u IS NULL AND r.isActive = true")
    List<Role> findUnassignedRoles();
    
    /**
     * Find roles created by specific user
     */
    List<Role> findByCreatedBy(String createdBy);
    
    /**
     * Find roles with workflow steps assigned
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.workflowSteps ws WHERE r.isActive = true")
    List<Role> findRolesWithWorkflowSteps();
}
