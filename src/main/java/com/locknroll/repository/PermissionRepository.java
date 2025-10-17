package com.locknroll.repository;

import com.locknroll.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Permission entity operations
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * Find permission by name
     */
    Optional<Permission> findByName(String name);
    
    /**
     * Check if permission name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find active permissions
     */
    List<Permission> findByIsActiveTrue();
    
    /**
     * Find permissions by resource
     */
    List<Permission> findByResource(String resource);
    
    /**
     * Find permissions by action
     */
    List<Permission> findByAction(String action);
    
    /**
     * Find permissions by resource and action
     */
    List<Permission> findByResourceAndAction(String resource, String action);
    
    /**
     * Find permissions by role name
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName AND p.isActive = true")
    List<Permission> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Find permissions by multiple role names
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r WHERE r.name IN :roleNames AND p.isActive = true")
    List<Permission> findByRoleNames(@Param("roleNames") List<String> roleNames);
    
    /**
     * Find permissions with specific resource and action for a role
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName AND p.resource = :resource AND p.action = :action AND p.isActive = true")
    List<Permission> findByRoleNameAndResourceAndAction(@Param("roleName") String roleName, @Param("resource") String resource, @Param("action") String action);
    
    /**
     * Count roles by permission
     */
    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p.id = :permissionId AND r.isActive = true")
    Long countActiveRolesByPermissionId(@Param("permissionId") Long permissionId);
    
    /**
     * Find permissions with no roles assigned
     */
    @Query("SELECT p FROM Permission p LEFT JOIN p.roles r WHERE r IS NULL AND p.isActive = true")
    List<Permission> findUnassignedPermissions();
    
    /**
     * Find permissions created by specific user
     */
    List<Permission> findByCreatedBy(String createdBy);
}
