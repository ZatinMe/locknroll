package com.locknroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for ParallelProcessingGroup entity
 */
public class ParallelProcessingGroupDto {

    @NotBlank(message = "Group name is required")
    private String groupName;

    @NotNull(message = "Workflow ID is required")
    private Long workflowId;

    private Integer groupOrder = 0;

    @NotNull(message = "Completion strategy is required")
    private String completionStrategy; // ALL_COMPLETE, ANY_COMPLETE, MAJORITY, CUSTOM

    private Integer requiredCompletionCount;

    private Boolean isActive = true;

    private String description;

    // Constructors
    public ParallelProcessingGroupDto() {}

    public ParallelProcessingGroupDto(String groupName, Long workflowId, String completionStrategy) {
        this.groupName = groupName;
        this.workflowId = workflowId;
        this.completionStrategy = completionStrategy;
    }

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
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

    @Override
    public String toString() {
        return "ParallelProcessingGroupDto{" +
                "groupName='" + groupName + '\'' +
                ", workflowId=" + workflowId +
                ", groupOrder=" + groupOrder +
                ", completionStrategy='" + completionStrategy + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
