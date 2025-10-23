package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Entity representing workflow timeouts
 */
@Entity
@Table(name = "workflow_timeouts")
public class WorkflowTimeout extends BaseEntity {

    @NotNull(message = "Workflow step is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStep workflowStep;

    @NotNull(message = "Timeout duration is required")
    @Positive(message = "Timeout duration must be positive")
    @Column(name = "timeout_duration_hours", nullable = false)
    private Integer timeoutDurationHours;

    @Column(name = "timeout_action", length = 50, nullable = false)
    private String timeoutAction; // AUTO_APPROVE, AUTO_REJECT, ESCALATE, NOTIFY

    @Column(name = "escalation_role", length = 50)
    private String escalationRole; // Role to escalate to if timeout action is ESCALATE

    @Column(name = "notification_message", length = 500)
    private String notificationMessage;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    // Constructors
    public WorkflowTimeout() {}

    public WorkflowTimeout(WorkflowStep workflowStep, Integer timeoutDurationHours, String timeoutAction) {
        this.workflowStep = workflowStep;
        this.timeoutDurationHours = timeoutDurationHours;
        this.timeoutAction = timeoutAction;
    }

    // Getters and Setters
    public WorkflowStep getWorkflowStep() {
        return workflowStep;
    }

    public void setWorkflowStep(WorkflowStep workflowStep) {
        this.workflowStep = workflowStep;
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

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    @Override
    public String toString() {
        return "WorkflowTimeout{" +
                "id=" + getId() +
                ", timeoutDurationHours=" + timeoutDurationHours +
                ", timeoutAction='" + timeoutAction + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
