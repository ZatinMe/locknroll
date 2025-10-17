package com.locknroll.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for WorkflowStepDependency entity
 */
public class WorkflowStepDependencyDto {
    
    private Long id;
    
    @NotNull(message = "Parent step ID is required")
    private Long parentStepId;
    
    @NotNull(message = "Dependent step ID is required")
    private Long dependentStepId;
    
    private String parentStepName;
    private String dependentStepName;
    
    private String dependencyType; // e.g., "SEQUENTIAL", "PARALLEL", "CONDITIONAL"
    private String condition; // Optional condition for conditional dependencies
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    
    // Constructors
    public WorkflowStepDependencyDto() {}
    
    public WorkflowStepDependencyDto(Long parentStepId, Long dependentStepId, String dependencyType) {
        this.parentStepId = parentStepId;
        this.dependentStepId = dependentStepId;
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
    
    public Long getParentStepId() {
        return parentStepId;
    }
    
    public void setParentStepId(Long parentStepId) {
        this.parentStepId = parentStepId;
    }
    
    public Long getDependentStepId() {
        return dependentStepId;
    }
    
    public void setDependentStepId(Long dependentStepId) {
        this.dependentStepId = dependentStepId;
    }
    
    public String getParentStepName() {
        return parentStepName;
    }
    
    public void setParentStepName(String parentStepName) {
        this.parentStepName = parentStepName;
    }
    
    public String getDependentStepName() {
        return dependentStepName;
    }
    
    public void setDependentStepName(String dependentStepName) {
        this.dependentStepName = dependentStepName;
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
