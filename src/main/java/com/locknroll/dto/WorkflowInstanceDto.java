package com.locknroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO for WorkflowInstance entity
 */
public class WorkflowInstanceDto {
    
    private Long id;
    
    @NotNull(message = "Workflow ID is required")
    private Long workflowId;
    
    @NotBlank(message = "Entity type is required")
    @Size(max = 50, message = "Entity type must not exceed 50 characters")
    private String entityType; // e.g., "FRUIT", "SELLER", "ORDER"
    
    @NotNull(message = "Entity ID is required")
    private Long entityId; // ID of the entity being processed
    
    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status; // e.g., "PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED", "CANCELLED"
    
    private String workflowName;
    private String entityName; // Name of the entity being processed
    
    private String createdBy;
    private String updatedBy;
    
    // Timestamps (commented out to avoid LocalDateTime serialization issues)
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
    // private LocalDateTime startedAt;
    // private LocalDateTime completedAt;
    
    private List<TaskDto> tasks;
    
    // Constructors
    public WorkflowInstanceDto() {}
    
    public WorkflowInstanceDto(Long workflowId, String entityType, Long entityId, String status) {
        this.workflowId = workflowId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getWorkflowName() {
        return workflowName;
    }
    
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
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
    
    public List<TaskDto> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<TaskDto> tasks) {
        this.tasks = tasks;
    }
}
