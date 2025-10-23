package com.locknroll.service;

import com.locknroll.entity.*;
import com.locknroll.repository.*;
import com.locknroll.dto.*;
import com.locknroll.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Workflow Execution Engine - Core engine for executing approval workflows
 * Handles task generation, dependency management, and state transitions
 */
@Service
@Transactional
public class WorkflowExecutionEngine {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutionEngine.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private WorkflowStepRepository workflowStepRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDependencyRepository taskDependencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private CacheService cacheService;

    /**
     * Start a workflow execution for an entity
     */
    public WorkflowInstanceDto startWorkflow(String entityType, String entityId, String workflowName, String startedBy) {
        logger.info("Starting workflow '{}' for entity {}:{}", workflowName, entityType, entityId);

        // Get the workflow template
        Workflow workflow = workflowRepository.findByName(workflowName)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + workflowName));

        if (!workflow.getIsActive()) {
            throw new WorkflowException("Workflow is not active: " + workflowName);
        }

        // Check if workflow instance already exists
        Optional<WorkflowInstance> existingInstance = workflowInstanceRepository
                .findByEntityTypeAndEntityId(entityType, entityId);

        if (existingInstance.isPresent()) {
            WorkflowInstance instance = existingInstance.get();
            if (Arrays.asList("PENDING", "IN_PROGRESS").contains(instance.getStatus())) {
                throw new WorkflowException("Workflow already in progress for entity: " + entityId);
            }
        }

        // Create workflow instance
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflow(workflow);
        workflowInstance.setEntityType(entityType);
        workflowInstance.setEntityId(entityId);
        workflowInstance.setStatus("PENDING");
        workflowInstance.setCurrentStepOrder(0);
        workflowInstance.setStartedAt(LocalDateTime.now());
        workflowInstance.setIsActive(true);
        workflowInstance.setCreatedBy(startedBy);

        WorkflowInstance savedInstance = workflowInstanceRepository.save(workflowInstance);
        logger.info("Workflow instance created with ID: {}", savedInstance.getId());

        // Generate tasks for the workflow
        try {
            logger.info("About to generate tasks for workflow instance: {}", savedInstance.getId());
            generateTasksForWorkflow(savedInstance);
            //generate tasks in "PENDING"
            logger.info("Tasks generated successfully for workflow instance: {}", savedInstance.getId());
        } catch (Exception e) {
            logger.error("Failed to generate tasks for workflow instance: {}", savedInstance.getId(), e);
            throw e;
        }

        // Start the first step
        try {
            logger.info("About to start first step for workflow instance: {}", savedInstance.getId());
            startNextStep(savedInstance);
            // lets start all the steps for doing whatsoever is needed  | but some tasks are dependent so will be in pending state rest independent step tasts -> READY
            logger.info("First step started successfully for workflow instance: {}", savedInstance.getId());
        } catch (Exception e) {
            logger.error("Failed to start first step for workflow instance: {}", savedInstance.getId(), e);
            throw e;
        }

        // Send notification to all users involved in the workflow
        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrder(workflowInstance.getWorkflow().getId());
        for (WorkflowStep step : steps) {
            List<User> usersWithRole = userRepository.findByRoleName(step.getAssignedRoleName());
            for (User user : usersWithRole) {
                String message = String.format("New workflow '%s' started for %s %s", 
                    workflowName, entityType, entityId);
                notificationService.sendWorkflowStatusNotification(
                    user.getUsername(), workflowName, "STARTED", message);
            }
        }

        // Publish workflow started event
        eventPublisher.publishWorkflowStarted(entityType, entityId, savedInstance.getId().toString(), 
            workflowName, startedBy, startedBy);

        logger.info("Workflow '{}' started successfully for entity {}:{}", workflowName, entityType, entityId);
        return convertToDto(savedInstance);
    }

    /**
     * Generate tasks for a workflow instance based on workflow steps
     */
    private void generateTasksForWorkflow(WorkflowInstance workflowInstance) {
        logger.info("TASK GENERATION: Starting for workflow instance: {}", workflowInstance.getId());

        // Get all workflow steps ordered by step order
        List<WorkflowStep> steps = workflowStepRepository
                .findByWorkflowIdOrderByStepOrder(workflowInstance.getWorkflow().getId());
        logger.info("TASK GENERATION: Found {} steps for workflow", steps.size());

        for (WorkflowStep step : steps) {
            if (!step.getIsActive()) {
                continue;
            }

            // Find users with the required role
            List<User> usersWithRole = userRepository.findByRoleName(step.getAssignedRoleName());
            
            if (usersWithRole.isEmpty()) {
                logger.warn("No users found with role: {}", step.getAssignedRoleName());
                continue;
            }

            // Create tasks for each user with the required role
            logger.info("TASK GENERATION: Creating tasks for {} users with role {}", usersWithRole.size(), step.getAssignedRoleName());
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

                // Set due date (e.g., 7 days from now)
                task.setDueDate(LocalDateTime.now().plusDays(7));

                Task savedTask = taskRepository.save(task);
                logger.info("TASK GENERATION: Created task ID={} for user {} ({})", savedTask.getId(), user.getUsername(), step.getName());
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + user.getId());
                logger.info("Invalidated cache for user: {} after creating task", user.getId());
                
                // Publish task created event
                eventPublisher.publishTaskCreated(savedTask.getId().toString(), step.getName(), 
                    user.getId().toString(), user.getUsername(), workflowInstance.getId().toString());
            }
        }

        // Set up task dependencies
        setupTaskDependencies(workflowInstance, steps);
    }

    /**
     * Set up task dependencies based on workflow step dependencies
     */
    private void setupTaskDependencies(WorkflowInstance workflowInstance, List<WorkflowStep> steps) {
        logger.debug("Setting up task dependencies for workflow instance: {}", workflowInstance.getId());

        // Get all tasks for this workflow instance
        List<Task> tasks = taskRepository.findByWorkflowInstanceId(workflowInstance.getId());

        // Create a map of step order to tasks
        Map<Integer, List<Task>> stepTasks = tasks.stream()
                .collect(Collectors.groupingBy(task -> {
                    // Find the step order for this task
                    return steps.stream()
                            .filter(step -> step.getName().equals(task.getTitle()))
                            .findFirst()
                            .map(WorkflowStep::getStepOrder)
                            .orElse(0);
                }));
//     {1->fin 2-> qual 3-> man}
        // Set up dependencies: each step depends on the previous step
        for (int i = 1; i <= steps.size(); i++) {
            List<Task> currentStepTasks = stepTasks.get(i);
            List<Task> previousStepTasks = stepTasks.get(i - 1);

            if (currentStepTasks != null && previousStepTasks != null) {
                for (Task currentTask : currentStepTasks) {
                    for (Task previousTask : previousStepTasks) {
                        TaskDependency dependency = new TaskDependency();
                        dependency.setParentTask(previousTask);
                        dependency.setDependentTask(currentTask);
                        dependency.setDependencyType("SEQUENTIAL");
                        dependency.setCreatedBy("system");

                        // Save dependency
                        taskDependencyRepository.save(dependency);
                        logger.debug("Created dependency: {} -> {}", previousTask.getId(), currentTask.getId());
                    }
                }
            }
        }
    }

    /**
     * Start the next step in the workflow
     */
    public void startNextStep(WorkflowInstance workflowInstance) {
        logger.debug("Starting next step for workflow instance: {}", workflowInstance.getId());

        // Get the next step
        List<WorkflowStep> steps = workflowStepRepository
                .findByWorkflowIdOrderByStepOrder(workflowInstance.getWorkflow().getId());

        int nextStepOrder = workflowInstance.getCurrentStepOrder() + 1;
        
        if (nextStepOrder > steps.size()) {
            // Workflow completed
            completeWorkflow(workflowInstance);
            return;
        }

        // Find the next step
        Optional<WorkflowStep> nextStep = steps.stream()
                .filter(step -> step.getStepOrder() == nextStepOrder)
                .findFirst();

        if (nextStep.isPresent()) {
            // Update workflow instance
            workflowInstance.setCurrentStepOrder(nextStepOrder);
            workflowInstance.setStatus("IN_PROGRESS");
            workflowInstanceRepository.save(workflowInstance);

            // Activate tasks for this step
            activateTasksForStep(workflowInstance, nextStep.get());
        }
    }

    /**
     * Activate tasks for a specific step
     */
    private void activateTasksForStep(WorkflowInstance workflowInstance, WorkflowStep step) {
        logger.debug("Activating tasks for step: {}", step.getName());

        // Get all tasks for this step
        List<Task> stepTasks = taskRepository.findByWorkflowInstanceIdAndStepName(
                workflowInstance.getId(), step.getName());

        for (Task task : stepTasks) {
            // Check if all dependencies are completed
            if (areTaskDependenciesCompleted(task)) {
                task.setStatus("READY");
                task.setUpdatedBy("system");
                taskRepository.save(task);
                logger.debug("Activated task: {}", task.getId());
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + task.getAssignedTo().getId());
            } else {
                task.setStatus("BLOCKED");
                task.setUpdatedBy("system");
                taskRepository.save(task);
                logger.debug("Task {} is blocked by dependencies", task.getId());
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + task.getAssignedTo().getId());
            }
        }
    }

    /**
     * Check if all dependencies for a task are completed
     */
    private boolean areTaskDependenciesCompleted(Task task) {
        // Get all parent dependencies
        List<TaskDependency> dependencies = taskDependencyRepository.findByDependentTaskId(task.getId());
        
        for (TaskDependency dependency : dependencies) {
            Task parentTask = dependency.getParentTask();
            if (!Arrays.asList("COMPLETED", "APPROVED").contains(parentTask.getStatus())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Complete a workflow
     */
    public void completeWorkflow(WorkflowInstance workflowInstance) {
        logger.info("Completing workflow instance: {}", workflowInstance.getId());

        workflowInstance.setStatus("COMPLETED");
        workflowInstance.setCompletedAt(LocalDateTime.now());
        workflowInstanceRepository.save(workflowInstance);

        // Notify relevant parties
        notifyWorkflowCompletion(workflowInstance);
    }

    /**
     * Reject a workflow
     */
    public void rejectWorkflow(WorkflowInstance workflowInstance, String reason, String rejectedBy) {
        logger.info("Rejecting workflow instance: {} - Reason: {}", workflowInstance.getId(), reason);

        workflowInstance.setStatus("REJECTED");
        workflowInstance.setCancelledAt(LocalDateTime.now());
        workflowInstance.setCancellationReason(reason);
        workflowInstanceRepository.save(workflowInstance);

        // Cancel all pending tasks
        cancelPendingTasks(workflowInstance);

        // Notify relevant parties
        notifyWorkflowRejection(workflowInstance, reason);
    }

    /**
     * Cancel pending tasks for a workflow
     */
    private void cancelPendingTasks(WorkflowInstance workflowInstance) {
        List<Task> pendingTasks = taskRepository.findByWorkflowInstanceIdAndStatus(
                workflowInstance.getId(), "PENDING");

        for (Task task : pendingTasks) {
            task.setStatus("CANCELLED");
            task.setUpdatedBy("system");
            taskRepository.save(task);
            
            // Invalidate cache for the assigned user
            cacheService.evict("user:tasks:" + task.getAssignedTo().getId());
        }
    }

    /**
     * Notify workflow completion
     */
    private void notifyWorkflowCompletion(WorkflowInstance workflowInstance) {
        // TODO: Implement notification system (email, websocket, etc.)
        logger.info("Workflow completed for entity: {}:{}", 
                workflowInstance.getEntityType(), workflowInstance.getEntityId());
    }

    /**
     * Notify workflow rejection
     */
    private void notifyWorkflowRejection(WorkflowInstance workflowInstance, String reason) {
        // TODO: Implement notification system (email, websocket, etc.)
        logger.info("Workflow rejected for entity: {}:{} - Reason: {}", 
                workflowInstance.getEntityType(), workflowInstance.getEntityId(), reason);
    }

    /**
     * Get workflow execution status
     */
    public WorkflowExecutionStatusDto getWorkflowStatus(Long workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findById(workflowInstanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow instance not found: " + workflowInstanceId));

        List<Task> tasks = taskRepository.findByWorkflowInstanceId(workflowInstanceId);

        WorkflowExecutionStatusDto status = new WorkflowExecutionStatusDto();
        status.setWorkflowInstanceId(workflowInstanceId);
        status.setStatus(workflowInstance.getStatus());
        status.setCurrentStep(workflowInstance.getCurrentStepOrder());
        status.setTotalSteps(workflowStepRepository.findByWorkflowIdOrderByStepOrder(workflowInstance.getWorkflow().getId()).size());
        status.setTotalTasks((long) tasks.size());
        status.setCompletedTasks(tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count());
        status.setPendingTasks(tasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count());
        status.setBlockedTasks(tasks.stream().filter(t -> "BLOCKED".equals(t.getStatus())).count());

        return status;
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
