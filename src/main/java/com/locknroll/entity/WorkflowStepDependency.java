package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * WorkflowStepDependency entity representing dependencies between workflow steps
 */
@Entity
@Table(name = "workflow_step_dependencies")
public class WorkflowStepDependency extends BaseEntity {
    
    @NotNull(message = "Parent step is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_step_id", nullable = false)
    private WorkflowStep parentStep;
    
    @NotNull(message = "Dependent step is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_step_id", nullable = false)
    private WorkflowStep dependentStep;
    
    @Column(name = "dependency_type", length = 50)
    private String dependencyType; // e.g., "SEQUENTIAL", "PARALLEL", "CONDITIONAL"
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public WorkflowStepDependency() {}
    
    public WorkflowStepDependency(WorkflowStep parentStep, WorkflowStep dependentStep, String dependencyType) {
        this.parentStep = parentStep;
        this.dependentStep = dependentStep;
        this.dependencyType = dependencyType;
    }
    
    // Getters and Setters
    public WorkflowStep getParentStep() {
        return parentStep;
    }
    
    public void setParentStep(WorkflowStep parentStep) {
        this.parentStep = parentStep;
    }
    
    public WorkflowStep getDependentStep() {
        return dependentStep;
    }
    
    public void setDependentStep(WorkflowStep dependentStep) {
        this.dependentStep = dependentStep;
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
        return "WorkflowStepDependency{" +
                "id=" + getId() +
                ", parentStep=" + (parentStep != null ? parentStep.getName() : null) +
                ", dependentStep=" + (dependentStep != null ? dependentStep.getName() : null) +
                ", dependencyType='" + dependencyType + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
