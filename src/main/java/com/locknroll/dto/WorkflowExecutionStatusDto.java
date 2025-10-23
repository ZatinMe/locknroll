package com.locknroll.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for workflow execution status
 */
public class WorkflowExecutionStatusDto {

    @NotNull
    private Long workflowInstanceId;

    @NotNull
    private String status; // PENDING, IN_PROGRESS, COMPLETED, REJECTED, CANCELLED

    private Integer currentStep;

    private Integer totalSteps;

    private Long totalTasks;

    private Long completedTasks;

    private Long pendingTasks;

    private Long blockedTasks;

    // Constructors
    public WorkflowExecutionStatusDto() {}

    // Getters and Setters
    public Long getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(Long workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(Long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public Long getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(Long pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public Long getBlockedTasks() {
        return blockedTasks;
    }

    public void setBlockedTasks(Long blockedTasks) {
        this.blockedTasks = blockedTasks;
    }

    @Override
    public String toString() {
        return "WorkflowExecutionStatusDto{" +
                "workflowInstanceId=" + workflowInstanceId +
                ", status='" + status + '\'' +
                ", currentStep=" + currentStep +
                ", totalSteps=" + totalSteps +
                ", totalTasks=" + totalTasks +
                ", completedTasks=" + completedTasks +
                ", pendingTasks=" + pendingTasks +
                ", blockedTasks=" + blockedTasks +
                '}';
    }
}
