package com.locknroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO for Task entity
 */
public class TaskDto {
    
    private Long id;
    
    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must not exceed 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Task status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status; // e.g., "PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED", "CANCELLED"
    
    @NotNull(message = "Workflow instance ID is required")
    private Long workflowInstanceId;
    
    private Long workflowStepId;
    private String workflowStepName;
    
    private Long assignedToId;
    private String assignedToUsername;
    private String assignedToFullName;
    
    private Long assignedRoleId;
    private String assignedRoleName;
    
    private String priority; // e.g., "LOW", "MEDIUM", "HIGH", "URGENT"
    private String taskType; // e.g., "APPROVAL", "REVIEW", "NOTIFICATION"
    
    private String createdBy;
    private String updatedBy;
    
    // Timestamps (commented out to avoid LocalDateTime serialization issues)
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
    // private LocalDateTime dueDate;
    // private LocalDateTime completedAt;
    
    private List<TaskDependencyDto> parentDependencies;
    private List<TaskDependencyDto> dependentDependencies;
    
    // Constructors
    public TaskDto() {}
    
    public TaskDto(String title, String description, String status, Long workflowInstanceId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.workflowInstanceId = workflowInstanceId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getWorkflowInstanceId() {
        return workflowInstanceId;
    }
    
    public void setWorkflowInstanceId(Long workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }
    
    public Long getWorkflowStepId() {
        return workflowStepId;
    }
    
    public void setWorkflowStepId(Long workflowStepId) {
        this.workflowStepId = workflowStepId;
    }
    
    public String getWorkflowStepName() {
        return workflowStepName;
    }
    
    public void setWorkflowStepName(String workflowStepName) {
        this.workflowStepName = workflowStepName;
    }
    
    public Long getAssignedToId() {
        return assignedToId;
    }
    
    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }
    
    public String getAssignedToUsername() {
        return assignedToUsername;
    }
    
    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }
    
    public String getAssignedToFullName() {
        return assignedToFullName;
    }
    
    public void setAssignedToFullName(String assignedToFullName) {
        this.assignedToFullName = assignedToFullName;
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
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    public void setTaskType(String taskType) {
        this.taskType = taskType;
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
    
    public List<TaskDependencyDto> getParentDependencies() {
        return parentDependencies;
    }
    
    public void setParentDependencies(List<TaskDependencyDto> parentDependencies) {
        this.parentDependencies = parentDependencies;
    }
    
    public List<TaskDependencyDto> getDependentDependencies() {
        return dependentDependencies;
    }
    
    public void setDependentDependencies(List<TaskDependencyDto> dependentDependencies) {
        this.dependentDependencies = dependentDependencies;
    }
}
