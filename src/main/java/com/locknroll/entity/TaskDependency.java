package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * TaskDependency entity representing dependencies between tasks
 */
@Entity
@Table(name = "task_dependencies")
public class TaskDependency extends BaseEntity {
    
    @NotNull(message = "Parent task is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", nullable = false)
    private Task parentTask;
    
    @NotNull(message = "Dependent task is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_task_id", nullable = false)
    private Task dependentTask;
    
    @Column(name = "dependency_type", length = 50)
    private String dependencyType; // e.g., "FINISH_TO_START", "START_TO_START", "FINISH_TO_FINISH"
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public TaskDependency() {}
    
    public TaskDependency(Task parentTask, Task dependentTask, String dependencyType) {
        this.parentTask = parentTask;
        this.dependentTask = dependentTask;
        this.dependencyType = dependencyType;
    }
    
    // Getters and Setters
    public Task getParentTask() {
        return parentTask;
    }
    
    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }
    
    public Task getDependentTask() {
        return dependentTask;
    }
    
    public void setDependentTask(Task dependentTask) {
        this.dependentTask = dependentTask;
    }
    
    public String getDependencyType() {
        return dependencyType;
    }
    
    public void setDependencyType(String dependencyType) {
        this.dependencyType = dependencyType;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "TaskDependency{" +
                "id=" + getId() +
                ", parentTask=" + (parentTask != null ? parentTask.getTitle() : null) +
                ", dependentTask=" + (dependentTask != null ? dependentTask.getTitle() : null) +
                ", dependencyType='" + dependencyType + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
