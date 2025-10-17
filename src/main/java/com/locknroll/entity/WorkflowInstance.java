package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * WorkflowInstance entity representing an instance of a workflow execution
 */
@Entity
@Table(name = "workflow_instances")
public class WorkflowInstance extends BaseEntity {
    
    @NotNull(message = "Workflow is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;
    
    @Column(name = "entity_id")
    private Long entityId; // ID of the entity being processed (e.g., fruit ID)
    
    @Size(max = 50, message = "Entity type must not exceed 50 characters")
    @Column(name = "entity_type", length = 50)
    private String entityType; // Type of entity (e.g., "FRUIT", "SELLER")
    
    @Column(name = "status", length = 50)
    private String status; // e.g., "ACTIVE", "COMPLETED", "CANCELLED", "FAILED"
    
    @Column(name = "current_step_order")
    private Integer currentStepOrder; // Current step being executed
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // One-to-many relationship with tasks
    @OneToMany(mappedBy = "workflowInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();
    
    // One-to-many relationship with approvals
    @OneToMany(mappedBy = "workflowInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Approval> approvals = new ArrayList<>();
    
    // Constructors
    public WorkflowInstance() {}
    
    public WorkflowInstance(Workflow workflow, Long entityId, String entityType) {
        this.workflow = workflow;
        this.entityId = entityId;
        this.entityType = entityType;
        this.status = "ACTIVE";
        this.startedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Workflow getWorkflow() {
        return workflow;
    }
    
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getCurrentStepOrder() {
        return currentStepOrder;
    }
    
    public void setCurrentStepOrder(Integer currentStepOrder) {
        this.currentStepOrder = currentStepOrder;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public List<Task> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
    
    public List<Approval> getApprovals() {
        return approvals;
    }
    
    public void setApprovals(List<Approval> approvals) {
        this.approvals = approvals;
    }
    
    // Helper methods
    public void addTask(Task task) {
        tasks.add(task);
        task.setWorkflowInstance(this);
    }
    
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setWorkflowInstance(null);
    }
    
    public void addApproval(Approval approval) {
        approvals.add(approval);
        approval.setWorkflowInstance(this);
    }
    
    public void removeApproval(Approval approval) {
        approvals.remove(approval);
        approval.setWorkflowInstance(null);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status) && isActive;
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }
    
    public void cancel(String reason) {
        this.status = "CANCELLED";
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
    
    @Override
    public String toString() {
        return "WorkflowInstance{" +
                "id=" + getId() +
                ", workflow=" + (workflow != null ? workflow.getName() : null) +
                ", entityId='" + entityId + '\'' +
                ", entityType='" + entityType + '\'' +
                ", status='" + status + '\'' +
                ", currentStepOrder=" + currentStepOrder +
                ", startedAt=" + startedAt +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
