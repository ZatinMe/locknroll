package com.locknroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for WorkflowCondition entity
 */
public class WorkflowConditionDto {

    @NotBlank(message = "Condition name is required")
    private String conditionName;

    @NotBlank(message = "Condition expression is required")
    private String conditionExpression;

    @NotNull(message = "Workflow step ID is required")
    private Long workflowStepId;

    private Boolean isActive = true;

    private Integer priority = 0;

    private String description;

    // Constructors
    public WorkflowConditionDto() {}

    public WorkflowConditionDto(String conditionName, String conditionExpression, Long workflowStepId) {
        this.conditionName = conditionName;
        this.conditionExpression = conditionExpression;
        this.workflowStepId = workflowStepId;
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

    public Long getWorkflowStepId() {
        return workflowStepId;
    }

    public void setWorkflowStepId(Long workflowStepId) {
        this.workflowStepId = workflowStepId;
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
        return "WorkflowConditionDto{" +
                "conditionName='" + conditionName + '\'' +
                ", conditionExpression='" + conditionExpression + '\'' +
                ", workflowStepId=" + workflowStepId +
                ", isActive=" + isActive +
                ", priority=" + priority +
                '}';
    }
}
