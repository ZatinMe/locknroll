package com.locknroll.security;

import com.locknroll.entity.Role;
import com.locknroll.entity.User;
import com.locknroll.entity.Task;
import com.locknroll.entity.WorkflowInstance;
import com.locknroll.entity.Fruit;
import com.locknroll.security.CustomUserDetailsService.CustomUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role-based access control utility class
 */
@Component
public class RoleBasedAccessControl {

    private static final Logger logger = LoggerFactory.getLogger(RoleBasedAccessControl.class);

    /**
     * Get current authenticated user
     */
    public CustomUserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
            return (CustomUserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String roleName) {
        CustomUserPrincipal user = getCurrentUser();
        if (user == null) return false;
        
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleName));
    }

    /**
     * Check if current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roleNames) {
        CustomUserPrincipal user = getCurrentUser();
        if (user == null) return false;
        
        List<String> userRoles = user.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
        
        for (String roleName : roleNames) {
            if (userRoles.contains(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is back office user
     */
    public boolean isBackOffice() {
        return hasRole("BACKOFFICE");
    }

    /**
     * Check if current user is seller
     */
    public boolean isSeller() {
        return hasRole("SELLER");
    }

    /**
     * Check if current user is approver (Manager, Finance, or Quality)
     */
    public boolean isApprover() {
        return hasAnyRole("MANAGER", "FINANCE", "QUALITY");
    }

    /**
     * Check if user can access task
     */
    public boolean canAccessTask(Task task) {
        CustomUserPrincipal user = getCurrentUser();
        if (user == null) return false;
        
        // Admin and BackOffice can access all tasks
        if (isAdmin() || isBackOffice()) {
            return true;
        }
        
        // Task assignee can access their tasks
        if (task.getAssignedTo() != null && task.getAssignedTo().getId().equals(user.getUserId())) {
            return true;
        }
        
        // Approvers can access tasks assigned to their role
        if (isApprover() && task.getAssignedTo() != null) {
            User assignedUser = task.getAssignedTo();
            List<String> assignedUserRoles = assignedUser.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            
            // Check if assigned user has same role as current user
            List<String> currentUserRoles = user.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());
            
            return assignedUserRoles.stream().anyMatch(currentUserRoles::contains);
        }
        
        return false;
    }

    /**
     * Check if user can access workflow instance
     */
    public boolean canAccessWorkflowInstance(WorkflowInstance workflowInstance) {
        CustomUserPrincipal user = getCurrentUser();
        if (user == null) return false;
        
        // Admin and BackOffice can access all workflow instances
        if (isAdmin() || isBackOffice()) {
            return true;
        }
        
        // Sellers can access their own fruit workflow instances
        if (isSeller() && "FRUIT".equals(workflowInstance.getEntityType())) {
            // Check if the fruit was created by current user
            // This would require additional service call to get the fruit
            return true; // Simplified for now
        }
        
        // Approvers can access workflow instances they're involved in
        if (isApprover()) {
            return workflowInstance.getTasks().stream()
                    .anyMatch(task -> canAccessTask(task));
        }
        
        return false;
    }

    /**
     * Check if user can access fruit
     */
    public boolean canAccessFruit(Fruit fruit) {
        CustomUserPrincipal user = getCurrentUser();
        if (user == null) return false;
        
        // Admin and BackOffice can access all fruits
        if (isAdmin() || isBackOffice()) {
            return true;
        }
        
        // Sellers can access their own fruits
        if (isSeller() && fruit.getCreatedBy().equals(user.getUsername())) {
            return true;
        }
        
        // Approvers can access fruits in their approval workflow
        if (isApprover()) {
            // Check if there's a workflow instance for this fruit
            // and if the user has tasks in that workflow
            return true; // Simplified for now
        }
        
        return false;
    }

    /**
     * Check if user can modify fruit
     */
    public boolean canModifyFruit(Fruit fruit) {
        CustomUserPrincipal user = getCurrentUser();
        if (user == null) return false;
        
        // Admin and BackOffice can modify all fruits
        if (isAdmin() || isBackOffice()) {
            return true;
        }
        
        // Sellers can only modify their own draft fruits
        if (isSeller() && fruit.getCreatedBy().equals(user.getUsername()) && "DRAFT".equals(fruit.getStatus())) {
            return true;
        }
        
        return false;
    }

    /**
     * Check if user can approve/reject fruit
     */
    public boolean canApproveFruit(Fruit fruit) {
        CustomUserPrincipal user = getCurrentUser();
        if (user == null) return false;
        
        // Only approvers can approve fruits
        if (!isApprover()) {
            return false;
        }
        
        // Fruit must be in pending approval status
        if (!"PENDING_APPROVAL".equals(fruit.getStatus())) {
            return false;
        }
        
        // Check if user has pending tasks for this fruit's workflow
        // This would require additional service call
        return true; // Simplified for now
    }

    /**
     * Get user's roles as strings
     */
    public List<String> getUserRoles() {
        CustomUserPrincipal user = getCurrentUser();
        if (user == null) return List.of();
        
        return user.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }

    /**
     * Get current user ID
     */
    public Long getCurrentUserId() {
        CustomUserPrincipal user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * Get current username
     */
    public String getCurrentUsername() {
        CustomUserPrincipal user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
}
