package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Approval entity representing individual approvals in a workflow
 */
@Entity
@Table(name = "approvals")
public class Approval extends BaseEntity {
    
    @NotNull(message = "Workflow instance is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_instance_id", nullable = false)
    private WorkflowInstance workflowInstance;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
    
    @NotNull(message = "Approved by user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id", nullable = false)
    private User approvedBy;
    
    @Column(name = "decision", length = 20)
    private String decision; // e.g., "APPROVED", "REJECTED", "PENDING"
    
    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    @Column(length = 1000)
    private String comments;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public Approval() {}
    
    public Approval(WorkflowInstance workflowInstance, Task task, User approvedBy, String decision) {
        this.workflowInstance = workflowInstance;
        this.task = task;
        this.approvedBy = approvedBy;
        this.decision = decision;
        this.approvedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public WorkflowInstance getWorkflowInstance() {
        return workflowInstance;
    }
    
    public void setWorkflowInstance(WorkflowInstance workflowInstance) {
        this.workflowInstance = workflowInstance;
    }
    
    public Task getTask() {
        return task;
    }
    
    public void setTask(Task task) {
        this.task = task;
    }
    
    public User getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public String getDecision() {
        return decision;
    }
    
    public void setDecision(String decision) {
        this.decision = decision;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Helper methods
    public boolean isApproved() {
        return "APPROVED".equals(decision);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(decision);
    }
    
    public boolean isPending() {
        return "PENDING".equals(decision);
    }
    
    public void approve(String comments) {
        this.decision = "APPROVED";
        this.comments = comments;
        this.approvedAt = LocalDateTime.now();
    }
    
    public void reject(String comments) {
        this.decision = "REJECTED";
        this.comments = comments;
        this.approvedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Approval{" +
                "id=" + getId() +
                ", decision='" + decision + '\'' +
                ", approvedBy=" + (approvedBy != null ? approvedBy.getUsername() : null) +
                ", approvedAt=" + approvedAt +
                ", comments='" + comments + '\'' +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
