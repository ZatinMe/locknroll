package com.locknroll.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event class for workflow-related events
 */
public class WorkflowEvent {
    
    private String eventId;
    private String eventType;
    private String entityType;
    private String entityId;
    private String workflowInstanceId;
    private String workflowName;
    private String status;
    private String userId;
    private String username;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, Object> metadata;
    
    // Event types
    public static final String WORKFLOW_STARTED = "WORKFLOW_STARTED";
    public static final String WORKFLOW_COMPLETED = "WORKFLOW_COMPLETED";
    public static final String WORKFLOW_FAILED = "WORKFLOW_FAILED";
    public static final String TASK_CREATED = "TASK_CREATED";
    public static final String TASK_ASSIGNED = "TASK_ASSIGNED";
    public static final String TASK_COMPLETED = "TASK_COMPLETED";
    public static final String TASK_REJECTED = "TASK_REJECTED";
    public static final String STEP_COMPLETED = "STEP_COMPLETED";
    public static final String APPROVAL_GRANTED = "APPROVAL_GRANTED";
    public static final String APPROVAL_DENIED = "APPROVAL_DENIED";

    // Constructors
    public WorkflowEvent() {}

    public WorkflowEvent(String eventId, String eventType, String entityType, String entityId, 
                        String workflowInstanceId, String workflowName, String status, 
                        String userId, String username, String message, LocalDateTime timestamp, 
                        Map<String, Object> metadata) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.workflowInstanceId = workflowInstanceId;
        this.workflowName = workflowName;
        this.status = status;
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
