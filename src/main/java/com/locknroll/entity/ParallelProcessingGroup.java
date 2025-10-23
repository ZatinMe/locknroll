package com.locknroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Entity representing parallel processing groups for workflow steps
 */
@Entity
@Table(name = "parallel_processing_groups")
public class ParallelProcessingGroup extends BaseEntity {

    @NotBlank(message = "Group name is required")
    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @NotNull(message = "Workflow is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(name = "group_order", nullable = false)
    private Integer groupOrder = 0;

    @Column(name = "completion_strategy", length = 50, nullable = false)
    private String completionStrategy; // ALL_COMPLETE, ANY_COMPLETE, MAJORITY, CUSTOM

    @Column(name = "required_completion_count")
    private Integer requiredCompletionCount; // For CUSTOM strategy

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "description", length = 500)
    private String description;

    // One-to-many relationship with workflow steps
    @OneToMany(mappedBy = "parallelProcessingGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkflowStep> workflowSteps;

    // Constructors
    public ParallelProcessingGroup() {}

    public ParallelProcessingGroup(String groupName, Workflow workflow, String completionStrategy) {
        this.groupName = groupName;
        this.workflow = workflow;
        this.completionStrategy = completionStrategy;
    }

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Integer getGroupOrder() {
        return groupOrder;
    }

    public void setGroupOrder(Integer groupOrder) {
        this.groupOrder = groupOrder;
    }

    public String getCompletionStrategy() {
        return completionStrategy;
    }

    public void setCompletionStrategy(String completionStrategy) {
        this.completionStrategy = completionStrategy;
    }

    public Integer getRequiredCompletionCount() {
        return requiredCompletionCount;
    }

    public void setRequiredCompletionCount(Integer requiredCompletionCount) {
        this.requiredCompletionCount = requiredCompletionCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }

    public void setWorkflowSteps(List<WorkflowStep> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    @Override
    public String toString() {
        return "ParallelProcessingGroup{" +
                "id=" + getId() +
                ", groupName='" + groupName + '\'' +
                ", groupOrder=" + groupOrder +
                ", completionStrategy='" + completionStrategy + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
