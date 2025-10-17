package com.locknroll.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for TaskDependency entity
 */
public class TaskDependencyDto {
    
    private Long id;
    
    @NotNull(message = "Parent task ID is required")
    private Long parentTaskId;
    
    @NotNull(message = "Dependent task ID is required")
    private Long dependentTaskId;
    
    private String parentTaskTitle;
    private String dependentTaskTitle;
    
    private String dependencyType; // e.g., "SEQUENTIAL", "PARALLEL", "CONDITIONAL"
    private String condition; // Optional condition for conditional dependencies
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    
    // Constructors
    public TaskDependencyDto() {}
    
    public TaskDependencyDto(Long parentTaskId, Long dependentTaskId, String dependencyType) {
        this.parentTaskId = parentTaskId;
        this.dependentTaskId = dependentTaskId;
        this.dependencyType = dependencyType;
        this.isActive = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getParentTaskId() {
        return parentTaskId;
    }
    
    public void setParentTaskId(Long parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
    
    public Long getDependentTaskId() {
        return dependentTaskId;
    }
    
    public void setDependentTaskId(Long dependentTaskId) {
        this.dependentTaskId = dependentTaskId;
    }
    
    public String getParentTaskTitle() {
        return parentTaskTitle;
    }
    
    public void setParentTaskTitle(String parentTaskTitle) {
        this.parentTaskTitle = parentTaskTitle;
    }
    
    public String getDependentTaskTitle() {
        return dependentTaskTitle;
    }
    
    public void setDependentTaskTitle(String dependentTaskTitle) {
        this.dependentTaskTitle = dependentTaskTitle;
    }
    
    public String getDependencyType() {
        return dependencyType;
    }
    
    public void setDependencyType(String dependencyType) {
        this.dependencyType = dependencyType;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
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
}
