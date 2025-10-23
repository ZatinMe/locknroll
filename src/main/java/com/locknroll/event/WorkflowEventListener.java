package com.locknroll.event;

import com.locknroll.entity.Task;
import com.locknroll.entity.TaskDependency;
import com.locknroll.entity.WorkflowInstance;
import com.locknroll.repository.TaskRepository;
import com.locknroll.repository.TaskDependencyRepository;
import com.locknroll.repository.WorkflowInstanceRepository;
import com.locknroll.service.CacheService;
import com.locknroll.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event listener for workflow events
 */
@Component
public class WorkflowEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEventListener.class);
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TaskDependencyRepository taskDependencyRepository;
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = "workflow-events", groupId = "workflow-listeners")
    public void handleWorkflowEvent(WorkflowEvent event) {
        logger.info("Received workflow event: {} - {}", event.getEventType(), event.getMessage());
        
        switch (event.getEventType()) {
            case WorkflowEvent.WORKFLOW_STARTED:
                handleWorkflowStarted(event);
                break;
            case WorkflowEvent.WORKFLOW_COMPLETED:
                handleWorkflowCompleted(event);
                break;
            case WorkflowEvent.WORKFLOW_FAILED:
                handleWorkflowFailed(event);
                break;
            default:
                logger.debug("Unhandled workflow event type: {}", event.getEventType());
        }
    }

    @KafkaListener(topics = "task-events", groupId = "task-listeners")
    public void handleTaskEvent(WorkflowEvent event) {
        logger.info("Received task event: {} - {}", event.getEventType(), event.getMessage());
        
        switch (event.getEventType()) {
            case WorkflowEvent.TASK_CREATED:
                handleTaskCreated(event);
                break;
            case WorkflowEvent.TASK_COMPLETED:
                handleTaskCompleted(event);
                break;
            case WorkflowEvent.TASK_REJECTED:
                handleTaskRejected(event);
                break;
            default:
                logger.debug("Unhandled task event type: {}", event.getEventType());
        }
    }

    @KafkaListener(topics = "approval-events", groupId = "approval-listeners")
    public void handleApprovalEvent(WorkflowEvent event) {
        logger.info("Received approval event: {} - {}", event.getEventType(), event.getMessage());
        
        switch (event.getEventType()) {
            case WorkflowEvent.APPROVAL_GRANTED:
                handleApprovalGranted(event);
                break;
            case WorkflowEvent.APPROVAL_DENIED:
                handleApprovalDenied(event);
                break;
            default:
                logger.debug("Unhandled approval event type: {}", event.getEventType());
        }
    }

    private void handleWorkflowStarted(WorkflowEvent event) {
        logger.info("Workflow started: {} for entity {}:{}", 
            event.getWorkflowName(), event.getEntityType(), event.getEntityId());
        // Here you could trigger notifications, update dashboards, etc.
    }

    private void handleWorkflowCompleted(WorkflowEvent event) {
        logger.info("Workflow completed: {} for entity {}:{}", 
            event.getWorkflowName(), event.getEntityType(), event.getEntityId());
        // Here you could trigger final notifications, update entity status, etc.
    }

    private void handleWorkflowFailed(WorkflowEvent event) {
        logger.warn("Workflow failed: {} for entity {}:{}", 
            event.getWorkflowName(), event.getEntityType(), event.getEntityId());
        // Here you could trigger failure notifications, rollback actions, etc.
    }

    private void handleTaskCreated(WorkflowEvent event) {
        logger.info("Task created: {} assigned to {}", 
            event.getMetadata().get("taskTitle"), event.getUsername());
        // Here you could trigger task notifications, update user dashboards, etc.
    }

    @Transactional
    private void handleTaskCompleted(WorkflowEvent event) {
        logger.info("Processing task completion event: {} by {}", 
            event.getMetadata().get("taskTitle"), event.getUsername());
        
        try {
            Long taskId = Long.parseLong(event.getEntityId());
            String status = event.getStatus();
            
            // Find the completed task
            Task completedTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
            
            logger.info("Task {} completed with status: {}", completedTask.getTitle(), status);
            
            // Update dependent tasks
            updateDependentTasksFromEvent(completedTask);
            
            // Check workflow instance completion
            checkWorkflowInstanceCompletionFromEvent(completedTask.getWorkflowInstance().getId());
            
            logger.info("Successfully processed task completion for task: {}", taskId);
            
        } catch (Exception e) {
            logger.error("Error processing task completion event: {}", e.getMessage(), e);
            // In a production system, you might want to send this to a dead letter queue
        }
    }

    @Transactional
    private void handleTaskRejected(WorkflowEvent event) {
        logger.info("Processing task rejection event: {} by {}", 
            event.getMetadata().get("taskTitle"), event.getUsername());
        
        try {
            Long taskId = Long.parseLong(event.getEntityId());
            
            // Find the rejected task
            Task rejectedTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
            
            logger.info("Task {} rejected", rejectedTask.getTitle());
            
            // Update dependent tasks (they may need to be cancelled or blocked)
            updateDependentTasksFromEvent(rejectedTask);
            
            // Check if workflow should be cancelled due to rejection
            checkWorkflowCancellationFromEvent(rejectedTask.getWorkflowInstance().getId());
            
            logger.info("Successfully processed task rejection for task: {}", taskId);
            
        } catch (Exception e) {
            logger.error("Error processing task rejection event: {}", e.getMessage(), e);
        }
    }

    private void handleApprovalGranted(WorkflowEvent event) {
        logger.info("Approval granted: {} for entity {}:{} by {}", 
            event.getEntityType(), event.getEntityId(), event.getUsername());
        // Here you could trigger approval notifications, update entity status, etc.
    }

    private void handleApprovalDenied(WorkflowEvent event) {
        logger.info("Approval denied: {} for entity {}:{} by {} - Reason: {}", 
            event.getEntityType(), event.getEntityId(), event.getUsername(), 
            event.getMetadata().get("reason"));
        // Here you could trigger denial notifications, update entity status, etc.
    }
    
    /**
     * Update dependent tasks when parent task completes/rejects (Event-driven)
     */
    @Transactional
    private void updateDependentTasksFromEvent(Task completedTask) {
        logger.info("Event-driven: Updating dependent tasks for completed task: {}", completedTask.getId());
        
        // Find all tasks that depend on this completed task
        List<TaskDependency> dependencies = taskDependencyRepository.findByParentTaskId(completedTask.getId());
        
        for (TaskDependency dependency : dependencies) {
            Task dependentTask = dependency.getDependentTask();
            
            // Check if all dependencies for this dependent task are now satisfied
            if (areAllDependenciesSatisfiedFromEvent(dependentTask)) {
                // Update dependent task to READY state
                dependentTask.setStatus("READY");
                dependentTask.setUpdatedBy("event-system");
                taskRepository.save(dependentTask);
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + dependentTask.getAssignedTo().getId());
                
                // Send notification to assigned user
                String assignedUsername = dependentTask.getAssignedTo().getUsername();
                String message = String.format("Task '%s' is now ready to start (dependencies satisfied)", dependentTask.getTitle());
                notificationService.sendTaskUpdateNotification(assignedUsername, dependentTask.getTitle(), "READY", message);
                
                logger.info("Event-driven: Activated dependent task: {} - {}", dependentTask.getId(), dependentTask.getTitle());
            } else {
                // Task is still blocked by other dependencies
                dependentTask.setStatus("BLOCKED");
                dependentTask.setUpdatedBy("event-system");
                taskRepository.save(dependentTask);
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + dependentTask.getAssignedTo().getId());
                
                logger.debug("Event-driven: Task {} is still blocked by other dependencies", dependentTask.getId());
            }
        }
    }
    
    /**
     * Check if all dependencies for a task are satisfied (Event-driven)
     */
    private boolean areAllDependenciesSatisfiedFromEvent(Task task) {
        List<TaskDependency> dependencies = taskDependencyRepository.findByDependentTaskId(task.getId());
        
        for (TaskDependency dependency : dependencies) {
            Task parentTask = dependency.getParentTask();
            if (!"COMPLETED".equals(parentTask.getStatus()) && !"APPROVED".equals(parentTask.getStatus())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if all tasks in a workflow instance are completed (Event-driven)
     */
    @Transactional
    private void checkWorkflowInstanceCompletionFromEvent(Long workflowInstanceId) {
        List<Task> pendingTasks = taskRepository.findByWorkflowInstanceIdAndStatusOrderByCreatedAt(workflowInstanceId, "PENDING");
        List<Task> inProgressTasks = taskRepository.findByWorkflowInstanceIdAndStatusOrderByCreatedAt(workflowInstanceId, "IN_PROGRESS");
        
        if (pendingTasks.isEmpty() && inProgressTasks.isEmpty()) {
            // All tasks are completed, update workflow instance status
            WorkflowInstance workflowInstance = workflowInstanceRepository.findById(workflowInstanceId)
                    .orElseThrow(() -> new RuntimeException("Workflow instance not found: " + workflowInstanceId));
            
            workflowInstance.setStatus("COMPLETED");
            workflowInstance.setCompletedAt(LocalDateTime.now());
            workflowInstance.setUpdatedBy("event-system");
            
            workflowInstanceRepository.save(workflowInstance);
            logger.info("Event-driven: Workflow instance {} completed", workflowInstanceId);
            
            // Send completion notification
            String message = String.format("Workflow instance %s has been completed", workflowInstanceId);
            notificationService.sendSystemNotification("Workflow Completed", message, "HIGH");
        }
    }
    
    /**
     * Check if workflow should be cancelled due to task rejection (Event-driven)
     */
    @Transactional
    private void checkWorkflowCancellationFromEvent(Long workflowInstanceId) {
        // Check if any critical tasks were rejected
        List<Task> rejectedTasks = taskRepository.findByWorkflowInstanceIdAndStatus(workflowInstanceId, "REJECTED");
        
        if (!rejectedTasks.isEmpty()) {
            // Check if any rejected tasks are critical (you might want to add a critical flag to tasks)
            boolean hasCriticalRejection = rejectedTasks.stream()
                .anyMatch(task -> "CRITICAL".equals(task.getPriority()) || "HIGH".equals(task.getPriority()));
            
            if (hasCriticalRejection) {
                // Cancel the workflow instance
                WorkflowInstance workflowInstance = workflowInstanceRepository.findById(workflowInstanceId)
                        .orElseThrow(() -> new RuntimeException("Workflow instance not found: " + workflowInstanceId));
                
                workflowInstance.setStatus("CANCELLED");
                workflowInstance.setCancelledAt(LocalDateTime.now());
                workflowInstance.setCancellationReason("Critical task rejected");
                workflowInstance.setUpdatedBy("event-system");
                
                workflowInstanceRepository.save(workflowInstance);
                logger.info("Event-driven: Workflow instance {} cancelled due to critical task rejection", workflowInstanceId);
                
                // Cancel all pending tasks
                cancelPendingTasksFromEvent(workflowInstanceId);
            }
        }
    }
    
    /**
     * Cancel pending tasks for a workflow (Event-driven)
     */
    @Transactional
    private void cancelPendingTasksFromEvent(Long workflowInstanceId) {
        List<Task> pendingTasks = taskRepository.findByWorkflowInstanceIdAndStatus(workflowInstanceId, "PENDING");
        
        for (Task task : pendingTasks) {
            task.setStatus("CANCELLED");
            task.setUpdatedBy("event-system");
            taskRepository.save(task);
            
            // Invalidate cache for the assigned user
            cacheService.evict("user:tasks:" + task.getAssignedTo().getId());
            
            // Send cancellation notification
            String message = String.format("Task '%s' has been cancelled due to workflow cancellation", task.getTitle());
            notificationService.sendTaskUpdateNotification(
                task.getAssignedTo().getUsername(), task.getTitle(), "CANCELLED", message);
        }
        
        logger.info("Event-driven: Cancelled {} pending tasks for workflow instance {}", pendingTasks.size(), workflowInstanceId);
    }
}

