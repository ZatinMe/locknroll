package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing workflow conditions for conditional approvals
 */
@Entity
@Table(name = "workflow_conditions")
public class WorkflowCondition extends BaseEntity {

    @NotBlank(message = "Condition name is required")
    @Column(name = "condition_name", length = 100, nullable = false)
    private String conditionName;

    @NotBlank(message = "Condition expression is required")
    @Column(name = "condition_expression", columnDefinition = "TEXT", nullable = false)
    private String conditionExpression; // JSON expression for evaluation

    @NotNull(message = "Workflow step is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStep workflowStep;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0; // Higher number = higher priority

    @Column(name = "description", length = 500)
    private String description;

    // Constructors
    public WorkflowCondition() {}

    public WorkflowCondition(String conditionName, String conditionExpression, WorkflowStep workflowStep) {
        this.conditionName = conditionName;
        this.conditionExpression = conditionExpression;
        this.workflowStep = workflowStep;
    }

    // Getters and Setters
    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public WorkflowStep getWorkflowStep() {
        return workflowStep;
    }

    public void setWorkflowStep(WorkflowStep workflowStep) {
        this.workflowStep = workflowStep;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "WorkflowCondition{" +
                "id=" + getId() +
                ", conditionName='" + conditionName + '\'' +
                ", isActive=" + isActive +
                ", priority=" + priority +
                '}';
    }
}
