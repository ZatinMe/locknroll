package com.locknroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO for Workflow entity
 */
public class WorkflowDto {
    
    private Long id;
    
    @NotBlank(message = "Workflow name is required")
    @Size(max = 100, message = "Workflow name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotBlank(message = "Entity type is required")
    @Size(max = 50, message = "Entity type must not exceed 50 characters")
    private String entityType; // e.g., "FRUIT", "SELLER", "ORDER"
    
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    
    private List<WorkflowStepDto> steps;
    
    // Constructors
    public WorkflowDto() {}
    
    public WorkflowDto(String name, String description, String entityType) {
        this.name = name;
        this.description = description;
        this.entityType = entityType;
        this.isActive = true;
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
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
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
    
    public List<WorkflowStepDto> getSteps() {
        return steps;
    }
    
    public void setSteps(List<WorkflowStepDto> steps) {
        this.steps = steps;
    }
}
