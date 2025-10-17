package com.locknroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for Role entity
 */
public class RoleDto {
    
    private Long id;
    
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    private String name;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    private Boolean isActive;
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    private Set<String> permissions;
    private Set<PermissionDto> permissionDetails;
    private Long userCount;
    
    // Constructors
    public RoleDto() {}
    
    public RoleDto(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    // public LocalDateTime getCreatedAt() {
    //     return createdAt;
    // }
    
    // public void setCreatedAt(LocalDateTime createdAt) {
    //     this.createdAt = createdAt;
    // }
    
    // public LocalDateTime getUpdatedAt() {
    //     return updatedAt;
    // }
    
    // public void setUpdatedAt(LocalDateTime updatedAt) {
    //     this.updatedAt = updatedAt;
    // }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public Set<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
    
    public Set<PermissionDto> getPermissionDetails() {
        return permissionDetails;
    }
    
    public void setPermissionDetails(Set<PermissionDto> permissionDetails) {
        this.permissionDetails = permissionDetails;
    }
    
    public Long getUserCount() {
        return userCount;
    }
    
    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }
    
    @Override
    public String toString() {
        return "RoleDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", userCount=" + userCount +
                '}';
    }
}
