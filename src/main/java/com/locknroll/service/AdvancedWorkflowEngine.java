package com.locknroll.service;

import com.locknroll.dto.WorkflowInstanceDto;
import com.locknroll.entity.*;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.WorkflowException;
import com.locknroll.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced workflow execution engine with conditional approvals, parallel processing, and timeouts
 */
@Service
public class AdvancedWorkflowEngine {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedWorkflowEngine.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private WorkflowStepRepository workflowStepRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WorkflowConditionRepository workflowConditionRepository;

    @Autowired
    private WorkflowTimeoutRepository workflowTimeoutRepository;

    @Autowired
    private ParallelProcessingGroupRepository parallelProcessingGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CacheService cacheService;

    /**
     * Start an advanced workflow with conditional logic and parallel processing
     */
    @Transactional
    public WorkflowInstanceDto startAdvancedWorkflow(String entityType, String entityId, String workflowName, String startedBy, Map<String, Object> context) {
        logger.info("Starting advanced workflow '{}' for entityType: {}, entityId: {}", workflowName, entityType, entityId);

        // Find the workflow definition
        Workflow workflow = workflowRepository.findByName(workflowName)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + workflowName));

        // Check if workflow instance already exists
        Optional<WorkflowInstance> existingInstance = workflowInstanceRepository
                .findByEntityTypeAndEntityId(entityType, entityId);

        if (existingInstance.isPresent()) {
            throw new WorkflowException("Workflow instance already exists for entityType: " + entityType + ", entityId: " + entityId);
        }

        // Create workflow instance
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflow(workflow);
        workflowInstance.setEntityType(entityType);
        workflowInstance.setEntityId(entityId);
        workflowInstance.setStatus("PENDING");
        workflowInstance.setCurrentStepOrder(0);
        workflowInstance.setStartedAt(LocalDateTime.now());
        workflowInstance.setCreatedBy(startedBy);
        workflowInstance.setIsActive(true);

        WorkflowInstance savedInstance = workflowInstanceRepository.save(workflowInstance);

        // Process workflow with advanced features
        processAdvancedWorkflow(savedInstance, context);

        logger.info("Advanced workflow '{}' started successfully for entity {}:{}", workflowName, entityType, entityId);
        return convertToDto(savedInstance);
    }

    /**
     * Process workflow with advanced features
     */
    private void processAdvancedWorkflow(WorkflowInstance workflowInstance, Map<String, Object> context) {
        // Get parallel processing groups
        List<ParallelProcessingGroup> groups = parallelProcessingGroupRepository
                .findActiveGroupsByWorkflowIdOrderByGroupOrder(workflowInstance.getWorkflow().getId());

        if (groups.isEmpty()) {
            // No parallel processing groups, use sequential processing
            processSequentialWorkflow(workflowInstance, context);
        } else {
            // Process parallel groups
            processParallelGroups(workflowInstance, groups, context);
        }
    }

    /**
     * Process workflow steps in parallel groups
     */
    private void processParallelGroups(WorkflowInstance workflowInstance, List<ParallelProcessingGroup> groups, Map<String, Object> context) {
        for (ParallelProcessingGroup group : groups) {
            logger.info("Processing parallel group: {}", group.getGroupName());
            
            // Get steps in this group
            List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrder(workflowInstance.getWorkflow().getId())
                    .stream()
                    .filter(step -> group.equals(step.getParallelProcessingGroup()))
                    .collect(Collectors.toList());

            // Evaluate conditions for each step
            List<WorkflowStep> activeSteps = evaluateConditions(steps, context);

            // Create tasks for active steps
            createTasksForSteps(workflowInstance, activeSteps);

            // Set up parallel processing
            setupParallelProcessing(workflowInstance, group, activeSteps);
        }
    }

    /**
     * Process workflow steps sequentially
     */
    private void processSequentialWorkflow(WorkflowInstance workflowInstance, Map<String, Object> context) {
        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrder(workflowInstance.getWorkflow().getId());
        
        // Evaluate conditions for each step
        List<WorkflowStep> activeSteps = evaluateConditions(steps, context);

        // Create tasks for active steps
        createTasksForSteps(workflowInstance, activeSteps);

        // Set up sequential processing
        setupSequentialProcessing(workflowInstance, activeSteps);
    }

    /**
     * Evaluate conditions for workflow steps
     */
    private List<WorkflowStep> evaluateConditions(List<WorkflowStep> steps, Map<String, Object> context) {
        List<WorkflowStep> activeSteps = new ArrayList<>();

        for (WorkflowStep step : steps) {
            // Get conditions for this step
            List<WorkflowCondition> conditions = workflowConditionRepository
                    .findActiveConditionsByStepIdOrderByPriority(step.getId());

            boolean shouldExecute = true;

            if (!conditions.isEmpty()) {
                // Evaluate conditions
                for (WorkflowCondition condition : conditions) {
                    if (!evaluateCondition(condition, context)) {
                        shouldExecute = false;
                        logger.debug("Step {} skipped due to condition: {}", step.getName(), condition.getConditionName());
                        break;
                    }
                }
            }

            if (shouldExecute) {
                activeSteps.add(step);
                logger.debug("Step {} will be executed", step.getName());
            }
        }

        return activeSteps;
    }

    /**
     * Evaluate a single condition
     */
    private boolean evaluateCondition(WorkflowCondition condition, Map<String, Object> context) {
        try {
            // Simple condition evaluation - in a real implementation, you might use a rule engine
            String expression = condition.getConditionExpression();
            
            // Example: Check if a field exists in context
            if (expression.contains("entityType")) {
                String expectedType = extractValue(expression, "entityType");
                return expectedType.equals(context.get("entityType"));
            }
            
            if (expression.contains("userRole")) {
                String expectedRole = extractValue(expression, "userRole");
                return expectedRole.equals(context.get("userRole"));
            }

            // Default to true if no specific conditions match
            return true;
        } catch (Exception e) {
            logger.error("Error evaluating condition {}: {}", condition.getConditionName(), e.getMessage());
            return false;
        }
    }

    /**
     * Extract value from condition expression
     */
    private String extractValue(String expression, String key) {
        // Simple extraction - in a real implementation, use a proper expression parser
        if (expression.contains(key + "=")) {
            String[] parts = expression.split(key + "=");
            if (parts.length > 1) {
                return parts[1].split("[,\\s]")[0];
            }
        }
        return "";
    }

    /**
     * Create tasks for workflow steps
     */
    private void createTasksForSteps(WorkflowInstance workflowInstance, List<WorkflowStep> steps) {
        for (WorkflowStep step : steps) {
            // Find users assigned to this role
            List<User> usersWithRole = userRepository.findByRoleName(step.getAssignedRoleName());

            if (usersWithRole.isEmpty()) {
                logger.warn("No users found with role: {}", step.getAssignedRoleName());
                continue;
            }

            // Create tasks for each user
            for (User user : usersWithRole) {
                Task task = new Task();
                task.setWorkflowInstance(workflowInstance);
                task.setWorkflowStep(step);
                task.setTitle(step.getName());
                task.setDescription(step.getDescription());
                task.setAssignedTo(user);
                task.setStatus("PENDING");
                task.setPriority("MEDIUM");
                task.setCreatedBy("system");
                task.setDueDate(LocalDateTime.now().plusDays(7));

                taskRepository.save(task);
                logger.debug("Created task {} for user {}", task.getId(), user.getUsername());
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + user.getId());
            }
        }
    }

    /**
     * Set up parallel processing for a group
     */
    private void setupParallelProcessing(WorkflowInstance workflowInstance, ParallelProcessingGroup group, List<WorkflowStep> steps) {
        logger.info("Setting up parallel processing for group: {}", group.getGroupName());
        
        // All steps in the group can run in parallel
        for (WorkflowStep step : steps) {
            // Activate tasks for this step
            activateTasksForStep(workflowInstance, step);
        }

        // Set up timeout monitoring for the group
        setupTimeoutMonitoring(workflowInstance, group, steps);
    }

    /**
     * Set up sequential processing
     */
    private void setupSequentialProcessing(WorkflowInstance workflowInstance, List<WorkflowStep> steps) {
        if (!steps.isEmpty()) {
            // Activate first step
            activateTasksForStep(workflowInstance, steps.get(0));
            
            // Set up timeout monitoring for the first step
            setupTimeoutMonitoring(workflowInstance, null, List.of(steps.get(0)));
        }
    }

    /**
     * Activate tasks for a specific step
     */
    private void activateTasksForStep(WorkflowInstance workflowInstance, WorkflowStep step) {
        List<Task> stepTasks = taskRepository.findByWorkflowInstanceIdAndStepName(
                workflowInstance.getId(), step.getName());

        for (Task task : stepTasks) {
            task.setStatus("READY");
            taskRepository.save(task);
            
            // Invalidate cache for the assigned user
            cacheService.evict("user:tasks:" + task.getAssignedTo().getId());
            
            // Send notification to assigned user
            String message = String.format("New task '%s' is ready for you", task.getTitle());
            notificationService.sendTaskUpdateNotification(
                    task.getAssignedTo().getUsername(), task.getTitle(), "READY", message);
        }
    }

    /**
     * Set up timeout monitoring
     */
    private void setupTimeoutMonitoring(WorkflowInstance workflowInstance, ParallelProcessingGroup group, List<WorkflowStep> steps) {
        for (WorkflowStep step : steps) {
            // Check if step has timeout configuration
            List<WorkflowTimeout> timeouts = workflowTimeoutRepository.findByWorkflowStepAndIsActive(step, true);
            
            for (WorkflowTimeout timeout : timeouts) {
                logger.info("Setting up timeout monitoring for step: {} with {} hours timeout", 
                        step.getName(), timeout.getTimeoutDurationHours());
                
                // In a real implementation, you would set up a scheduled task or use a job scheduler
                // For now, we'll just log it
            }
        }
    }

    /**
     * Handle timeout for a workflow step
     */
    @Async
    public void handleTimeout(WorkflowInstance workflowInstance, WorkflowStep step, WorkflowTimeout timeout) {
        logger.info("Handling timeout for step: {} in workflow instance: {}", step.getName(), workflowInstance.getId());

        try {
            switch (timeout.getTimeoutAction()) {
                case "AUTO_APPROVE":
                    autoApproveStep(workflowInstance, step);
                    break;
                case "AUTO_REJECT":
                    autoRejectStep(workflowInstance, step);
                    break;
                case "ESCALATE":
                    escalateStep(workflowInstance, step, timeout.getEscalationRole());
                    break;
                case "NOTIFY":
                    notifyTimeout(workflowInstance, step, timeout);
                    break;
                default:
                    logger.warn("Unknown timeout action: {}", timeout.getTimeoutAction());
            }
        } catch (Exception e) {
            logger.error("Error handling timeout for step {}: {}", step.getName(), e.getMessage());
        }
    }

    /**
     * Auto-approve a step
     */
    private void autoApproveStep(WorkflowInstance workflowInstance, WorkflowStep step) {
        List<Task> stepTasks = taskRepository.findByWorkflowInstanceIdAndStepName(
                workflowInstance.getId(), step.getName());

        for (Task task : stepTasks) {
            if ("PENDING".equals(task.getStatus()) || "READY".equals(task.getStatus())) {
                task.setStatus("COMPLETED");
                task.setCompletedAt(LocalDateTime.now());
                task.setUpdatedBy("system-timeout");
                taskRepository.save(task);
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + task.getAssignedTo().getId());

                // Send notification
                String message = String.format("Task '%s' was auto-approved due to timeout", task.getTitle());
                notificationService.sendTaskUpdateNotification(
                        task.getAssignedTo().getUsername(), task.getTitle(), "AUTO_APPROVED", message);
            }
        }

        logger.info("Auto-approved step: {} in workflow instance: {}", step.getName(), workflowInstance.getId());
    }

    /**
     * Auto-reject a step
     */
    private void autoRejectStep(WorkflowInstance workflowInstance, WorkflowStep step) {
        List<Task> stepTasks = taskRepository.findByWorkflowInstanceIdAndStepName(
                workflowInstance.getId(), step.getName());

        for (Task task : stepTasks) {
            if ("PENDING".equals(task.getStatus()) || "READY".equals(task.getStatus())) {
                task.setStatus("REJECTED");
                task.setCompletedAt(LocalDateTime.now());
                task.setUpdatedBy("system-timeout");
                taskRepository.save(task);
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + task.getAssignedTo().getId());

                // Send notification
                String message = String.format("Task '%s' was auto-rejected due to timeout", task.getTitle());
                notificationService.sendTaskUpdateNotification(
                        task.getAssignedTo().getUsername(), task.getTitle(), "AUTO_REJECTED", message);
            }
        }

        logger.info("Auto-rejected step: {} in workflow instance: {}", step.getName(), workflowInstance.getId());
    }

    /**
     * Escalate a step to a different role
     */
    private void escalateStep(WorkflowInstance workflowInstance, WorkflowStep step, String escalationRole) {
        logger.info("Escalating step: {} to role: {}", step.getName(), escalationRole);

        // Find users with the escalation role
        List<User> escalationUsers = userRepository.findByRoleName(escalationRole);

        if (escalationUsers.isEmpty()) {
            logger.warn("No users found with escalation role: {}", escalationRole);
            return;
        }

        // Create new tasks for escalation users
        for (User user : escalationUsers) {
            Task escalationTask = new Task();
            escalationTask.setWorkflowInstance(workflowInstance);
            escalationTask.setWorkflowStep(step);
            escalationTask.setTitle("ESCALATED: " + step.getName());
            escalationTask.setDescription("This task was escalated due to timeout");
            escalationTask.setAssignedTo(user);
            escalationTask.setStatus("READY");
            escalationTask.setPriority("HIGH");
            escalationTask.setCreatedBy("system-escalation");
            escalationTask.setDueDate(LocalDateTime.now().plusDays(3));

            taskRepository.save(escalationTask);
            
            // Invalidate cache for the assigned user
            cacheService.evict("user:tasks:" + escalationTask.getAssignedTo().getId());
            
            // Send notification
            String message = String.format("Escalated task '%s' assigned to you", escalationTask.getTitle());
            notificationService.sendTaskUpdateNotification(
                    user.getUsername(), escalationTask.getTitle(), "ESCALATED", message);
        }
    }

    /**
     * Notify about timeout
     */
    private void notifyTimeout(WorkflowInstance workflowInstance, WorkflowStep step, WorkflowTimeout timeout) {
        String message = timeout.getNotificationMessage() != null ? 
                timeout.getNotificationMessage() : 
                String.format("Step '%s' has timed out in workflow instance %d", step.getName(), workflowInstance.getId());

        // Send notification to all users involved in the workflow
        List<Task> allTasks = taskRepository.findByWorkflowInstanceId(workflowInstance.getId());
        Set<String> usernames = allTasks.stream()
                .map(task -> task.getAssignedTo().getUsername())
                .collect(Collectors.toSet());

        for (String username : usernames) {
            notificationService.sendWorkflowStatusNotification(username, 
                    workflowInstance.getWorkflow().getName(), "TIMEOUT", message);
        }
    }

    /**
     * Scheduled method to check for timeouts
     */
    @Scheduled(fixedRate = 300000) // Check every 5 minutes
    public void checkTimeouts() {
        logger.debug("Checking for workflow timeouts");

        try {
            List<WorkflowTimeout> overdueTimeouts = workflowTimeoutRepository.findOverdueTimeouts(LocalDateTime.now());

            for (WorkflowTimeout timeout : overdueTimeouts) {
                // Find active workflow instances for this step
                List<WorkflowInstance> activeInstances = workflowInstanceRepository
                        .findByWorkflowIdAndStatus(timeout.getWorkflowStep().getWorkflow().getId(), "IN_PROGRESS");

                for (WorkflowInstance instance : activeInstances) {
                    handleTimeout(instance, timeout.getWorkflowStep(), timeout);
                }

                // Update last checked time
                timeout.setLastChecked(LocalDateTime.now());
                workflowTimeoutRepository.save(timeout);
            }
        } catch (Exception e) {
            logger.error("Error checking timeouts: {}", e.getMessage());
        }
    }

    /**
     * Convert WorkflowInstance to DTO
     */
    private WorkflowInstanceDto convertToDto(WorkflowInstance workflowInstance) {
        WorkflowInstanceDto dto = new WorkflowInstanceDto();
        dto.setId(workflowInstance.getId());
        dto.setWorkflowId(workflowInstance.getWorkflow().getId());
        dto.setEntityType(workflowInstance.getEntityType());
        dto.setEntityId(workflowInstance.getEntityId());
        dto.setStatus(workflowInstance.getStatus());
        dto.setCreatedBy(workflowInstance.getCreatedBy());
        dto.setUpdatedBy(workflowInstance.getUpdatedBy());
        return dto;
    }
}
