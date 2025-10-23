package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * WorkflowStep entity representing individual steps in a workflow
 */
@Entity
@Table(name = "workflow_steps")
public class WorkflowStep extends BaseEntity {
    
    @NotBlank(message = "Step name is required")
    @Size(max = 100, message = "Step name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;
    
    @NotNull(message = "Step order is required")
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
    
    @Size(max = 50, message = "Step type must not exceed 50 characters")
    @Column(name = "step_type", length = 50)
    private String stepType; // e.g., "APPROVAL", "REVIEW", "NOTIFICATION"
    
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;
    
    @Column(name = "is_parallel", nullable = false)
    private Boolean isParallel = false; // Can run in parallel with other steps
    
    @Column(name = "timeout_hours")
    private Integer timeoutHours; // Timeout for this step
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Role assignment for this step
    @Column(name = "assigned_role_name", length = 50)
    private String assignedRoleName; // Role name that can execute this step
    
    // Many-to-one relationship with workflow
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;
    
    // Many-to-one relationship with assigned role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_role_id")
    private Role assignedRole;

    // Many-to-one relationship with parallel processing group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parallel_processing_group_id")
    private ParallelProcessingGroup parallelProcessingGroup;
    
    // One-to-many relationship with dependent steps
    @OneToMany(mappedBy = "parentStep", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkflowStepDependency> dependencies = new ArrayList<>();
    
    // One-to-many relationship with tasks created from this step
    @OneToMany(mappedBy = "workflowStep", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();
    
    // Constructors
    public WorkflowStep() {}
    
    public WorkflowStep(String name, String description, Integer stepOrder, String stepType) {
        this.name = name;
        this.description = description;
        this.stepOrder = stepOrder;
        this.stepType = stepType;
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
    
    public Integer getStepOrder() {
        return stepOrder;
    }
    
    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }
    
    public String getStepType() {
        return stepType;
    }
    
    public void setStepType(String stepType) {
        this.stepType = stepType;
    }
    
    public Boolean getIsRequired() {
        return isRequired;
    }
    
    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }
    
    public Boolean getIsParallel() {
        return isParallel;
    }
    
    public void setIsParallel(Boolean isParallel) {
        this.isParallel = isParallel;
    }
    
    public Integer getTimeoutHours() {
        return timeoutHours;
    }
    
    public void setTimeoutHours(Integer timeoutHours) {
        this.timeoutHours = timeoutHours;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getAssignedRoleName() {
        return assignedRoleName;
    }
    
    public void setAssignedRoleName(String assignedRoleName) {
        this.assignedRoleName = assignedRoleName;
    }

    public ParallelProcessingGroup getParallelProcessingGroup() {
        return parallelProcessingGroup;
    }

    public void setParallelProcessingGroup(ParallelProcessingGroup parallelProcessingGroup) {
        this.parallelProcessingGroup = parallelProcessingGroup;
    }
    
    public Workflow getWorkflow() {
        return workflow;
    }
    
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }
    
    public Role getAssignedRole() {
        return assignedRole;
    }
    
    public void setAssignedRole(Role assignedRole) {
        this.assignedRole = assignedRole;
    }
    
    public List<WorkflowStepDependency> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<WorkflowStepDependency> dependencies) {
        this.dependencies = dependencies;
    }
    
    public List<Task> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
    
    // Helper methods
    public void addDependency(WorkflowStepDependency dependency) {
        dependencies.add(dependency);
        dependency.setParentStep(this);
    }
    
    public void removeDependency(WorkflowStepDependency dependency) {
        dependencies.remove(dependency);
        dependency.setParentStep(null);
    }
    
    public void addTask(Task task) {
        tasks.add(task);
        task.setWorkflowStep(this);
    }
    
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setWorkflowStep(null);
    }
    
    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }
    
    public boolean isDependentOn(WorkflowStep step) {
        return dependencies.stream()
                .anyMatch(dep -> dep.getDependentStep().equals(step));
    }
    
    @Override
    public String toString() {
        return "WorkflowStep{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", stepOrder=" + stepOrder +
                ", stepType='" + stepType + '\'' +
                ", isRequired=" + isRequired +
                ", isParallel=" + isParallel +
                ", workflow=" + (workflow != null ? workflow.getName() : null) +
                ", assignedRole=" + (assignedRole != null ? assignedRole.getName() : null) +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
