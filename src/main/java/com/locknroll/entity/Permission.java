package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Permission entity representing system permissions
 */
@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Permission extends BaseEntity {
    
    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Column(length = 200)
    private String description;
    
    @Size(max = 50, message = "Resource must not exceed 50 characters")
    @Column(length = 50)
    private String resource; // e.g., "FRUIT", "USER", "WORKFLOW"
    
    @Size(max = 50, message = "Action must not exceed 50 characters")
    @Column(length = 50)
    private String action; // e.g., "CREATE", "READ", "UPDATE", "DELETE", "APPROVE"
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Many-to-many relationship with roles
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
    
    // Constructors
    public Permission() {}
    
    public Permission(String name, String description, String resource, String action) {
        this.name = name;
        this.description = description;
        this.resource = resource;
        this.action = action;
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
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    // Helper methods
    public void addRole(Role role) {
        this.roles.add(role);
        role.getPermissions().add(this);
    }
    
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getPermissions().remove(this);
    }
    
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
