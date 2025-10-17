package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Task entity representing individual tasks in a workflow
 */
@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {
    
    @NotNull(message = "Workflow instance is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_instance_id", nullable = false)
    private WorkflowInstance workflowInstance;
    
    @NotNull(message = "Workflow step is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStep workflowStep;
    
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;
    
    @Column(name = "status", length = 50)
    private String status; // e.g., "PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED", "CANCELLED"
    
    @Column(name = "priority", length = 20)
    private String priority; // e.g., "LOW", "MEDIUM", "HIGH", "URGENT"
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    @Column(length = 1000)
    private String comments;
    
    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // One-to-many relationship with task dependencies
    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskDependency> dependencies = new ArrayList<>();
    
    // One-to-many relationship with approvals
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Approval> approvals = new ArrayList<>();
    
    // Constructors
    public Task() {}
    
    public Task(WorkflowInstance workflowInstance, WorkflowStep workflowStep, String title, String description) {
        this.workflowInstance = workflowInstance;
        this.workflowStep = workflowStep;
        this.title = title;
        this.description = description;
        this.status = "PENDING";
        this.priority = "MEDIUM";
    }
    
    // Getters and Setters
    public WorkflowInstance getWorkflowInstance() {
        return workflowInstance;
    }
    
    public void setWorkflowInstance(WorkflowInstance workflowInstance) {
        this.workflowInstance = workflowInstance;
    }
    
    public WorkflowStep getWorkflowStep() {
        return workflowStep;
    }
    
    public void setWorkflowStep(WorkflowStep workflowStep) {
        this.workflowStep = workflowStep;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public User getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
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
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public List<TaskDependency> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<TaskDependency> dependencies) {
        this.dependencies = dependencies;
    }
    
    public List<Approval> getApprovals() {
        return approvals;
    }
    
    public void setApprovals(List<Approval> approvals) {
        this.approvals = approvals;
    }
    
    // Helper methods
    public void addDependency(TaskDependency dependency) {
        dependencies.add(dependency);
        dependency.setParentTask(this);
    }
    
    public void removeDependency(TaskDependency dependency) {
        dependencies.remove(dependency);
        dependency.setParentTask(null);
    }
    
    public void addApproval(Approval approval) {
        approvals.add(approval);
        approval.setTask(this);
    }
    
    public void removeApproval(Approval approval) {
        approvals.remove(approval);
        approval.setTask(null);
    }
    
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    public void start() {
        this.status = "IN_PROGRESS";
        this.startedAt = LocalDateTime.now();
    }
    
    public void complete(String comments) {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
        this.comments = comments;
    }
    
    public void reject(String reason) {
        this.status = "REJECTED";
        this.completedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }
    
    public void cancel() {
        this.status = "CANCELLED";
        this.completedAt = LocalDateTime.now();
    }
    
    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }
    
    public boolean isDependentOn(Task task) {
        return dependencies.stream()
                .anyMatch(dep -> dep.getDependentTask().equals(task));
    }
    
    @Override
    public String toString() {
        return "Task{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", assignedTo=" + (assignedTo != null ? assignedTo.getUsername() : null) +
                ", dueDate=" + dueDate +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
