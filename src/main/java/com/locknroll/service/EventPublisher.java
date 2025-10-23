package com.locknroll.service;

import com.locknroll.event.WorkflowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for publishing workflow events to Kafka
 */
@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    private KafkaTemplate<String, WorkflowEvent> kafkaTemplate;

    private static final String WORKFLOW_EVENTS_TOPIC = "workflow-events";
    private static final String TASK_EVENTS_TOPIC = "task-events";
    private static final String APPROVAL_EVENTS_TOPIC = "approval-events";

    /**
     * Publish workflow started event
     */
    public void publishWorkflowStarted(String entityType, String entityId, String workflowInstanceId, 
                                     String workflowName, String userId, String username) {
        WorkflowEvent event = createEvent(
            WorkflowEvent.WORKFLOW_STARTED,
            entityType,
            entityId,
            workflowInstanceId,
            workflowName,
            "STARTED",
            userId,
            username,
            String.format("Workflow '%s' started for %s %s", workflowName, entityType, entityId)
        );
        
        publishEvent(WORKFLOW_EVENTS_TOPIC, event);
    }

    /**
     * Publish workflow completed event
     */
    public void publishWorkflowCompleted(String entityType, String entityId, String workflowInstanceId, 
                                       String workflowName, String userId, String username) {
        WorkflowEvent event = createEvent(
            WorkflowEvent.WORKFLOW_COMPLETED,
            entityType,
            entityId,
            workflowInstanceId,
            workflowName,
            "COMPLETED",
            userId,
            username,
            String.format("Workflow '%s' completed for %s %s", workflowName, entityType, entityId)
        );
        
        publishEvent(WORKFLOW_EVENTS_TOPIC, event);
    }

    /**
     * Publish task created event
     */
    public void publishTaskCreated(String taskId, String taskTitle, String assignedUserId, 
                                 String assignedUsername, String workflowInstanceId) {
        WorkflowEvent event = new WorkflowEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(WorkflowEvent.TASK_CREATED);
        event.setEntityType("TASK");
        event.setEntityId(taskId);
        event.setWorkflowInstanceId(workflowInstanceId);
        event.setStatus("CREATED");
        event.setUserId(assignedUserId);
        event.setUsername(assignedUsername);
        event.setMessage(String.format("Task '%s' created and assigned to %s", taskTitle, assignedUsername));
        event.setTimestamp(LocalDateTime.now());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("taskTitle", taskTitle);
        metadata.put("taskId", taskId);
        event.setMetadata(metadata);
        
        publishEvent(TASK_EVENTS_TOPIC, event);
    }

    /**
     * Publish task completed event
     */
    public void publishTaskCompleted(String taskId, String taskTitle, String userId, 
                                   String username, String status) {
        WorkflowEvent event = new WorkflowEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(WorkflowEvent.TASK_COMPLETED);
        event.setEntityType("TASK");
        event.setEntityId(taskId);
        event.setStatus(status);
        event.setUserId(userId);
        event.setUsername(username);
        event.setMessage(String.format("Task '%s' %s by %s", taskTitle, status, username));
        event.setTimestamp(LocalDateTime.now());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("taskTitle", taskTitle);
        metadata.put("taskId", taskId);
        event.setMetadata(metadata);
        
        publishEvent(TASK_EVENTS_TOPIC, event);
    }

    /**
     * Publish approval granted event
     */
    public void publishApprovalGranted(String entityType, String entityId, String workflowInstanceId, 
                                     String approverUserId, String approverUsername) {
        WorkflowEvent event = createEvent(
            WorkflowEvent.APPROVAL_GRANTED,
            entityType,
            entityId,
            workflowInstanceId,
            null,
            "APPROVED",
            approverUserId,
            approverUsername,
            String.format("Approval granted for %s %s by %s", entityType, entityId, approverUsername)
        );
        
        publishEvent(APPROVAL_EVENTS_TOPIC, event);
    }

    /**
     * Publish approval denied event
     */
    public void publishApprovalDenied(String entityType, String entityId, String workflowInstanceId, 
                                    String approverUserId, String approverUsername, String reason) {
        WorkflowEvent event = createEvent(
            WorkflowEvent.APPROVAL_DENIED,
            entityType,
            entityId,
            workflowInstanceId,
            null,
            "REJECTED",
            approverUserId,
            approverUsername,
            String.format("Approval denied for %s %s by %s. Reason: %s", entityType, entityId, approverUsername, reason)
        );
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", reason);
        event.setMetadata(metadata);
        
        publishEvent(APPROVAL_EVENTS_TOPIC, event);
    }

    /**
     * Create a basic workflow event
     */
    private WorkflowEvent createEvent(String eventType, String entityType, String entityId, 
                                   String workflowInstanceId, String workflowName, String status,
                                   String userId, String username, String message) {
        WorkflowEvent event = new WorkflowEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setWorkflowInstanceId(workflowInstanceId);
        event.setWorkflowName(workflowName);
        event.setStatus(status);
        event.setUserId(userId);
        event.setUsername(username);
        event.setMessage(message);
        event.setTimestamp(LocalDateTime.now());
        event.setMetadata(new HashMap<>());
        
        return event;
    }

    /**
     * Publish event to Kafka
     */
    private void publishEvent(String topic, WorkflowEvent event) {
        try {
            kafkaTemplate.send(topic, event.getEventId(), event);
            logger.info("Published event {} to topic {}: {}", event.getEventType(), topic, event.getMessage());
        } catch (Exception e) {
            logger.error("Failed to publish event to Kafka: {}", e.getMessage(), e);
        }
    }
}

