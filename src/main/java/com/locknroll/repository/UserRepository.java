package com.locknroll.repository;

import com.locknroll.entity.User;
import com.locknroll.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username with roles eagerly fetched
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
    
    /**
     * Find user by email with roles eagerly fetched
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username or email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = true")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Find users by role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.isActive = true")
    List<User> findByRolesContaining(@Param("role") Role role);
    
    /**
     * Find users with specific role and active status
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = :isActive")
    List<User> findByRoleNameAndIsActive(@Param("roleName") String roleName, @Param("isActive") Boolean isActive);
    
    /**
     * Find users by multiple roles
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames AND u.isActive = true")
    List<User> findByRoleNames(@Param("roleNames") List<String> roleNames);
    
    /**
     * Find users with email verification status
     */
    List<User> findByIsEmailVerified(Boolean isEmailVerified);
    
    /**
     * Find users created by specific user
     */
    List<User> findByCreatedBy(String createdBy);
    
    /**
     * Find users with last login after specific date
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin > :date AND u.isActive = true")
    List<User> findActiveUsersWithRecentLogin(@Param("date") java.time.LocalDateTime date);
    
    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = true")
    Long countByRoleName(@Param("roleName") String roleName);
    
    /**
     * Find users with pending tasks
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.assignedTasks t WHERE t.status = 'PENDING' AND u.isActive = true")
    List<User> findUsersWithPendingTasks();
    
    /**
     * Find users with overdue tasks
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.assignedTasks t WHERE t.dueDate < CURRENT_TIMESTAMP AND t.status IN ('PENDING', 'IN_PROGRESS') AND u.isActive = true")
    List<User> findUsersWithOverdueTasks();
}
