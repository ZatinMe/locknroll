package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Role entity representing user roles in the system
 */
@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Role extends BaseEntity {
    
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Column(length = 200)
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Many-to-many relationship with users
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
    
    // Many-to-many relationship with permissions
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permissions",
               joinColumns = @JoinColumn(name = "role_id"),
               inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();
    
    // One-to-many relationship with workflow steps that can be assigned to this role
    @OneToMany(mappedBy = "assignedRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<WorkflowStep> workflowSteps = new HashSet<>();
    
    // Constructors
    public Role() {}
    
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Set<User> getUsers() {
        return users;
    }
    
    public void setUsers(Set<User> users) {
        this.users = users;
    }
    
    public Set<Permission> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
    
    public Set<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }
    
    public void setWorkflowSteps(Set<WorkflowStep> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }
    
    // Helper methods
    public void addUser(User user) {
        this.users.add(user);
        user.getRoles().add(this);
    }
    
    public void removeUser(User user) {
        this.users.remove(user);
        user.getRoles().remove(this);
    }
    
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }
    
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }
    
    public boolean hasPermission(String permissionName) {
        return permissions.stream().anyMatch(permission -> permission.getName().equals(permissionName));
    }
    
    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
