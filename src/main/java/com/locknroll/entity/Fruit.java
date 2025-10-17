package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Fruit Entity - Stored in PostgreSQL
 * Represents a fruit item with inventory tracking and approval workflow
 */
@Entity
@Table(name = "fruits")
public class Fruit extends BaseEntity {
    
    @NotBlank(message = "Name is required")
    @Column(nullable = false, unique = true)
    private String name;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "status", length = 50)
    private String status; // e.g., "DRAFT", "PENDING_APPROVAL", "APPROVED", "REJECTED", "ACTIVE"
    
    @Column(name = "submitted_by", length = 100)
    private String submittedBy; // Username of the user who submitted the fruit
    
    @Column(name = "submitted_at")
    private java.time.LocalDateTime submittedAt;
    
    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;
    
    @Column(name = "rejected_at")
    private java.time.LocalDateTime rejectedAt;
    
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
    
    // One-to-many relationship with workflow instances
    @OneToMany(mappedBy = "entityId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkflowInstance> workflowInstances = new ArrayList<>();
    
    // Constructors
    public Fruit() {}
    
    public Fruit(String name, BigDecimal price, Integer quantity, String description, String category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.category = category;
        this.status = "DRAFT";
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSubmittedBy() {
        return submittedBy;
    }
    
    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }
    
    public java.time.LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(java.time.LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public java.time.LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(java.time.LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public java.time.LocalDateTime getRejectedAt() {
        return rejectedAt;
    }
    
    public void setRejectedAt(java.time.LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public List<WorkflowInstance> getWorkflowInstances() {
        return workflowInstances;
    }
    
    public void setWorkflowInstances(List<WorkflowInstance> workflowInstances) {
        this.workflowInstances = workflowInstances;
    }
    
    // Helper methods
    public boolean isDraft() {
        return "DRAFT".equals(status);
    }
    
    public boolean isPendingApproval() {
        return "PENDING_APPROVAL".equals(status);
    }
    
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    public void submitForApproval(String submittedBy) {
        this.status = "PENDING_APPROVAL";
        this.submittedBy = submittedBy;
        this.submittedAt = java.time.LocalDateTime.now();
    }
    
    public void approve() {
        this.status = "APPROVED";
        this.approvedAt = java.time.LocalDateTime.now();
    }
    
    public void reject(String reason) {
        this.status = "REJECTED";
        this.rejectedAt = java.time.LocalDateTime.now();
        this.rejectionReason = reason;
    }
    
    public void activate() {
        this.status = "ACTIVE";
    }
    
    public void addWorkflowInstance(WorkflowInstance workflowInstance) {
        workflowInstances.add(workflowInstance);
    }
    
    public void removeWorkflowInstance(WorkflowInstance workflowInstance) {
        workflowInstances.remove(workflowInstance);
    }
    
    @Override
    public String toString() {
        return "Fruit{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", submittedBy='" + submittedBy + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}

