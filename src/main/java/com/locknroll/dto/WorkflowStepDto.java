package com.locknroll.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO for WorkflowStep entity
 */
public class WorkflowStepDto {
    
    private Long id;
    
    @NotNull(message = "Step order is required")
    private Integer stepOrder;
    
    @Size(max = 100, message = "Step name must not exceed 100 characters")
    private String stepName;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Step type is required")
    @Size(max = 50, message = "Step type must not exceed 50 characters")
    private String stepType; // e.g., "APPROVAL", "REVIEW", "NOTIFICATION"
    
    private Long assignedRoleId;
    private String assignedRoleName;
    
    private Boolean isRequired;
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    
    private List<WorkflowStepDependencyDto> parentDependencies;
    private List<WorkflowStepDependencyDto> dependentDependencies;
    
    // Constructors
    public WorkflowStepDto() {}
    
    public WorkflowStepDto(Integer stepOrder, String stepName, String stepType, Long assignedRoleId) {
        this.stepOrder = stepOrder;
        this.stepName = stepName;
        this.stepType = stepType;
        this.assignedRoleId = assignedRoleId;
        this.isRequired = true;
        this.isActive = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getStepOrder() {
        return stepOrder;
    }
    
    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }
    
    public String getStepName() {
        return stepName;
    }
    
    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStepType() {
        return stepType;
    }
    
    public void setStepType(String stepType) {
        this.stepType = stepType;
    }
    
    public Long getAssignedRoleId() {
        return assignedRoleId;
    }
    
    public void setAssignedRoleId(Long assignedRoleId) {
        this.assignedRoleId = assignedRoleId;
    }
    
    public String getAssignedRoleName() {
        return assignedRoleName;
    }
    
    public void setAssignedRoleName(String assignedRoleName) {
        this.assignedRoleName = assignedRoleName;
    }
    
    public Boolean getIsRequired() {
        return isRequired;
    }
    
    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
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
    
    public List<WorkflowStepDependencyDto> getParentDependencies() {
        return parentDependencies;
    }
    
    public void setParentDependencies(List<WorkflowStepDependencyDto> parentDependencies) {
        this.parentDependencies = parentDependencies;
    }
    
    public List<WorkflowStepDependencyDto> getDependentDependencies() {
        return dependentDependencies;
    }
    
    public void setDependentDependencies(List<WorkflowStepDependencyDto> dependentDependencies) {
        this.dependentDependencies = dependentDependencies;
    }
}
