package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Workflow entity representing approval workflows
 */
@Entity
@Table(name = "workflows", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Workflow extends BaseEntity {
    
    @NotBlank(message = "Workflow name is required")
    @Size(max = 100, message = "Workflow name must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;
    
    @Size(max = 50, message = "Entity type must not exceed 50 characters")
    @Column(name = "entity_type", length = 50)
    private String entityType; // e.g., "FRUIT", "SELLER", "PRODUCT"
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "version", nullable = false)
    private Integer version = 1;
    
    // One-to-many relationship with workflow steps
    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<WorkflowStep> steps = new ArrayList<>();
    
    // One-to-many relationship with workflow instances
    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkflowInstance> instances = new ArrayList<>();
    
    // Constructors
    public Workflow() {}
    
    public Workflow(String name, String description, String entityType) {
        this.name = name;
        this.description = description;
        this.entityType = entityType;
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
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public List<WorkflowStep> getSteps() {
        return steps;
    }
    
    public void setSteps(List<WorkflowStep> steps) {
        this.steps = steps;
    }
    
    public List<WorkflowInstance> getInstances() {
        return instances;
    }
    
    public void setInstances(List<WorkflowInstance> instances) {
        this.instances = instances;
    }
    
    // Helper methods
    public void addStep(WorkflowStep step) {
        steps.add(step);
        step.setWorkflow(this);
    }
    
    public void removeStep(WorkflowStep step) {
        steps.remove(step);
        step.setWorkflow(null);
    }
    
    public WorkflowStep getFirstStep() {
        return steps.isEmpty() ? null : steps.get(0);
    }
    
    public WorkflowStep getLastStep() {
        return steps.isEmpty() ? null : steps.get(steps.size() - 1);
    }
    
    public WorkflowStep getStepByOrder(Integer stepOrder) {
        return steps.stream()
                .filter(step -> step.getStepOrder().equals(stepOrder))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public String toString() {
        return "Workflow{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", entityType='" + entityType + '\'' +
                ", isActive=" + isActive +
                ", version=" + version +
                ", stepsCount=" + steps.size() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
