package com.locknroll.service;

import com.locknroll.dto.TaskDto;
import com.locknroll.entity.Task;
import com.locknroll.entity.TaskDependency;
import com.locknroll.entity.WorkflowInstance;
import com.locknroll.entity.WorkflowStep;
import com.locknroll.entity.User;
import com.locknroll.entity.Role;
import com.locknroll.repository.TaskRepository;
import com.locknroll.repository.WorkflowInstanceRepository;
import com.locknroll.repository.WorkflowStepRepository;
import com.locknroll.repository.UserRepository;
import com.locknroll.repository.RoleRepository;
import com.locknroll.repository.TaskDependencyRepository;
import com.locknroll.service.EventPublisher;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.InvalidTaskStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Task operations
 */
@Service
@Transactional
public class TaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    
    @Autowired
    private WorkflowStepRepository workflowStepRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private TaskDependencyRepository taskDependencyRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EventPublisher eventPublisher;
    
    /**
     * Create tasks for a workflow instance
     * This method creates tasks based on the workflow steps and assigns them to appropriate users
     */
    public List<TaskDto> createTasksForWorkflowInstance(Long workflowInstanceId) {
        logger.info("Creating tasks for workflow instance: {}", workflowInstanceId);
        
        WorkflowInstance workflowInstance = workflowInstanceRepository.findById(workflowInstanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow instance not found with id: " + workflowInstanceId));
        
        // Get workflow steps
        List<WorkflowStep> workflowSteps = workflowStepRepository.findByWorkflowIdOrderByStepOrder(workflowInstance.getWorkflow().getId());
        
        if (workflowSteps.isEmpty()) {
            throw new InvalidTaskStateException("No workflow steps found for workflow: " + workflowInstance.getWorkflow().getName());
        }
        
        List<Task> createdTasks = workflowSteps.stream()
                .map(step -> createTaskForStep(workflowInstance, step))
                .collect(Collectors.toList());
        
        // Create task dependencies based on workflow step dependencies
        createTaskDependencies(createdTasks, workflowSteps);
        
        List<TaskDto> taskDtos = createdTasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        logger.info("Created {} tasks for workflow instance: {}", createdTasks.size(), workflowInstanceId);
        return taskDtos;
    }
    
    /**
     * Create a task for a specific workflow step
     */
    private Task createTaskForStep(WorkflowInstance workflowInstance, WorkflowStep workflowStep) {
        // Find a user with the required role
        User assignedUser = findUserForRole(workflowStep.getAssignedRole());
        
        Task task = new Task();
        task.setTitle(workflowStep.getName());
        task.setDescription(workflowStep.getDescription());
        task.setStatus("PENDING");
        task.setWorkflowInstance(workflowInstance);
        task.setWorkflowStep(workflowStep);
        task.setAssignedTo(assignedUser);
        task.setPriority("MEDIUM"); // Default priority
        // task.setTaskType(workflowStep.getStepType()); // Task entity doesn't have taskType field
        task.setCreatedBy("system"); // TODO: Get from security context
        
        Task savedTask = taskRepository.save(task);
        logger.info("Created task: {} for user: {}", savedTask.getTitle(), assignedUser.getUsername());
        
        // Invalidate cache for the assigned user when new task is created
        cacheService.evict("user:tasks:" + assignedUser.getId());
        logger.info("Invalidated cache for user: {} after creating new task", assignedUser.getId());
        
        return savedTask;
    }
    
    /**
     * Find a user with the specified role
     */
    private User findUserForRole(Role role) {
        List<User> usersWithRole = userRepository.findByRolesContaining(role);
        
        if (usersWithRole.isEmpty()) {
            throw new ResourceNotFoundException("No users found with role: " + role.getName());
        }
        
        // For now, return the first user with the role
        // In a real system, you might want to implement load balancing or assignment logic
        return usersWithRole.get(0);
    }
    
    /**
     * Create task dependencies based on workflow step dependencies
     */
    private void createTaskDependencies(List<Task> tasks, List<WorkflowStep> workflowSteps) {
        // For now, create sequential dependencies based on step order
        // In a real system, you would use the workflow step dependencies
        for (int i = 1; i < tasks.size(); i++) {
            Task currentTask = tasks.get(i);
            Task previousTask = tasks.get(i - 1);
            
            TaskDependency dependency = new TaskDependency();
            dependency.setParentTask(previousTask);
            dependency.setDependentTask(currentTask);
            dependency.setDependencyType("SEQUENTIAL");
            dependency.setIsActive(true);
            dependency.setCreatedBy("system");
            
            // Save dependency (this will be handled by the Task entity's cascade settings)
            currentTask.getDependencies().add(dependency);
        }
    }
    
    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        return convertToDto(task);
    }
    
    /**
     * Get tasks by workflow instance ID
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByWorkflowInstanceId(Long workflowInstanceId) {
        List<Task> tasks = taskRepository.findByWorkflowInstanceIdOrderByCreatedAt(workflowInstanceId);
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks assigned to a user
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByUserId(Long userId) {
        logger.info("Fetching tasks for user: {}", userId);
        
                // Try to get from cache first
                Optional<List<TaskDto>> cachedTasks = cacheService.getCachedUserTasks(userId, 
                    new com.fasterxml.jackson.core.type.TypeReference<List<TaskDto>>() {});
        
        if (cachedTasks.isPresent()) {
            logger.debug("Returning cached tasks for user: {}", userId);
            return cachedTasks.get();
        }
        
        // If not in cache, fetch from database
        List<Task> tasks = taskRepository.findByAssignedToIdOrderByCreatedAt(userId);
        List<TaskDto> taskDtos = tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Cache the result
        cacheService.cacheUserTasks(userId, taskDtos);
        
        return taskDtos;
    }
    
    /**
     * Get pending tasks for a user
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getPendingTasksForUser(Long userId) {
        List<Task> tasks = taskRepository.findPendingTasksForUser(userId);
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get completed tasks for a user
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getCompletedTasksForUser(Long userId) {
        List<Task> tasks = taskRepository.findCompletedTasksForUser(userId);
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by role
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByRole(Long roleId) {
        List<Task> tasks = taskRepository.findPendingTasksByRole(roleId);
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Update task status
     */
    public TaskDto updateTaskStatus(Long taskId, String status, String comments) {
        logger.info("Updating task {} status to: {}", taskId, status);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Validate status transition
        validateStatusTransition(task.getStatus(), status);
        
        task.setStatus(status);
        task.setUpdatedBy("system"); // TODO: Get from security context
        
        if ("COMPLETED".equals(status) || "REJECTED".equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
        }
        
        Task savedTask = taskRepository.save(task);
        
        // Invalidate cache for the assigned user
        cacheService.evict("user:tasks:" + savedTask.getAssignedTo().getId());
        logger.info("Invalidated cache for user: {}", savedTask.getAssignedTo().getId());
        
        // Send notification to assigned user
        String assignedUsername = savedTask.getAssignedTo().getUsername();
        String message = String.format("Task '%s' status updated to %s", savedTask.getTitle(), status);
        notificationService.sendTaskUpdateNotification(assignedUsername, savedTask.getTitle(), status, message);
        
        // CRITICAL: Update dependent tasks when parent task completes/rejects
        if ("COMPLETED".equals(status) || "REJECTED".equals(status)) {
            updateDependentTasks(savedTask);
            
            // Emit task completion event
            eventPublisher.publishTaskCompleted(
                savedTask.getId().toString(),
                savedTask.getTitle(),
                savedTask.getAssignedTo().getId().toString(),
                assignedUsername,
                status
            );
        }
        
        // Check if all tasks in the workflow instance are completed
        checkWorkflowInstanceCompletion(savedTask.getWorkflowInstance().getId());
        
        logger.info("Updated task {} status to: {}", taskId, status);
        return convertToDto(savedTask);
    }
    
    /**
     * Validate status transition
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case "READY":
                if (!"IN_PROGRESS".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
                    throw new InvalidTaskStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case "IN_PROGRESS":
                if (!"COMPLETED".equals(newStatus) && !"REJECTED".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
                    throw new InvalidTaskStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case "COMPLETED":
            case "REJECTED":
            case "CANCELLED":
                throw new InvalidTaskStateException("Cannot change status from " + currentStatus);
            default:
                throw new InvalidTaskStateException("Unknown status: " + currentStatus);
        }
    }
    
    /**
     * Update dependent tasks when parent task completes/rejects
     */
    private void updateDependentTasks(Task completedTask) {
        logger.info("Updating dependent tasks for completed task: {}", completedTask.getId());
        
        // Find all tasks that depend on this completed task
        List<TaskDependency> dependencies = taskDependencyRepository.findByParentTaskId(completedTask.getId());
        
        for (TaskDependency dependency : dependencies) {
            Task dependentTask = dependency.getDependentTask();
            
            // Check if all dependencies for this dependent task are now satisfied
            if (areAllDependenciesSatisfied(dependentTask)) {
                // Update dependent task to READY state
                dependentTask.setStatus("READY");
                dependentTask.setUpdatedBy("system");
                taskRepository.save(dependentTask);
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + dependentTask.getAssignedTo().getId());
                
                // Send notification to assigned user
                String assignedUsername = dependentTask.getAssignedTo().getUsername();
                String message = String.format("Task '%s' is now ready to start (dependencies satisfied)", dependentTask.getTitle());
                notificationService.sendTaskUpdateNotification(assignedUsername, dependentTask.getTitle(), "READY", message);
                
                // Emit task ready event
                eventPublisher.publishTaskCreated(
                    dependentTask.getId().toString(),
                    dependentTask.getTitle(),
                    dependentTask.getAssignedTo().getId().toString(),
                    assignedUsername,
                    dependentTask.getWorkflowInstance().getId().toString()
                );
                
                logger.info("Activated dependent task: {} - {}", dependentTask.getId(), dependentTask.getTitle());
            } else {
                // Task is still blocked by other dependencies
                dependentTask.setStatus("BLOCKED");
                dependentTask.setUpdatedBy("system");
                taskRepository.save(dependentTask);
                
                // Invalidate cache for the assigned user
                cacheService.evict("user:tasks:" + dependentTask.getAssignedTo().getId());
                
                logger.debug("Task {} is still blocked by other dependencies", dependentTask.getId());
            }
        }
    }
    
    /**
     * Check if all dependencies for a task are satisfied
     */
    private boolean areAllDependenciesSatisfied(Task task) {
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
     * Check if all tasks in a workflow instance are completed
     */
    private void checkWorkflowInstanceCompletion(Long workflowInstanceId) {
        List<Task> pendingTasks = taskRepository.findByWorkflowInstanceIdAndStatusOrderByCreatedAt(workflowInstanceId, "PENDING");
        List<Task> inProgressTasks = taskRepository.findByWorkflowInstanceIdAndStatusOrderByCreatedAt(workflowInstanceId, "IN_PROGRESS");
        
        if (pendingTasks.isEmpty() && inProgressTasks.isEmpty()) {
            // All tasks are completed, update workflow instance status
            WorkflowInstance workflowInstance = workflowInstanceRepository.findById(workflowInstanceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workflow instance not found with id: " + workflowInstanceId));
            
            workflowInstance.setStatus("COMPLETED");
            workflowInstance.setCompletedAt(LocalDateTime.now());
            workflowInstance.setUpdatedBy("system");
            
            workflowInstanceRepository.save(workflowInstance);
            logger.info("Workflow instance {} completed", workflowInstanceId);
        }
    }
    
    /**
     * Get ready tasks (tasks that can be started)
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getReadyTasks(Long workflowInstanceId) {
        List<Task> tasks = taskRepository.findReadyTasks(workflowInstanceId);
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get blocked tasks (tasks waiting for dependencies)
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getBlockedTasks(Long workflowInstanceId) {
        List<Task> tasks = taskRepository.findBlockedTasks(workflowInstanceId);
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert Task entity to DTO
     */
    public TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        // dto.setTaskType(task.getTaskType()); // Task entity doesn't have taskType field
        dto.setCreatedBy(task.getCreatedBy());
        dto.setUpdatedBy(task.getUpdatedBy());
        
        if (task.getWorkflowInstance() != null) {
            dto.setWorkflowInstanceId(task.getWorkflowInstance().getId());
        }
        
        if (task.getWorkflowStep() != null) {
            dto.setWorkflowStepId(task.getWorkflowStep().getId());
            dto.setWorkflowStepName(task.getWorkflowStep().getName());
            
            if (task.getWorkflowStep().getAssignedRole() != null) {
                dto.setAssignedRoleId(task.getWorkflowStep().getAssignedRole().getId());
                dto.setAssignedRoleName(task.getWorkflowStep().getAssignedRole().getName());
            }
        }
        
        if (task.getAssignedTo() != null) {
            dto.setAssignedToId(task.getAssignedTo().getId());
            dto.setAssignedToUsername(task.getAssignedTo().getUsername());
            dto.setAssignedToFullName(task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName());
        }
        
        return dto;
    }
}
