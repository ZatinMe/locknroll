package com.locknroll.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for WorkflowTimeout entity
 */
public class WorkflowTimeoutDto {

    @NotNull(message = "Workflow step ID is required")
    private Long workflowStepId;

    @NotNull(message = "Timeout duration is required")
    @Positive(message = "Timeout duration must be positive")
    private Integer timeoutDurationHours;

    @NotNull(message = "Timeout action is required")
    private String timeoutAction; // AUTO_APPROVE, AUTO_REJECT, ESCALATE, NOTIFY

    private String escalationRole;

    private String notificationMessage;

    private Boolean isActive = true;

    // Constructors
    public WorkflowTimeoutDto() {}

    public WorkflowTimeoutDto(Long workflowStepId, Integer timeoutDurationHours, String timeoutAction) {
        this.workflowStepId = workflowStepId;
        this.timeoutDurationHours = timeoutDurationHours;
        this.timeoutAction = timeoutAction;
    }

    // Getters and Setters
    public Long getWorkflowStepId() {
        return workflowStepId;
    }

    public void setWorkflowStepId(Long workflowStepId) {
        this.workflowStepId = workflowStepId;
    }

    public Integer getTimeoutDurationHours() {
        return timeoutDurationHours;
    }

    public void setTimeoutDurationHours(Integer timeoutDurationHours) {
        this.timeoutDurationHours = timeoutDurationHours;
    }

    public String getTimeoutAction() {
        return timeoutAction;
    }

    public void setTimeoutAction(String timeoutAction) {
        this.timeoutAction = timeoutAction;
    }

    public String getEscalationRole() {
        return escalationRole;
    }

    public void setEscalationRole(String escalationRole) {
        this.escalationRole = escalationRole;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "WorkflowTimeoutDto{" +
                "workflowStepId=" + workflowStepId +
                ", timeoutDurationHours=" + timeoutDurationHours +
                ", timeoutAction='" + timeoutAction + '\'' +
                ", escalationRole='" + escalationRole + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
