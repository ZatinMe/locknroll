package com.locknroll.controller;

import com.locknroll.dto.TaskDto;
import com.locknroll.service.TaskService;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.InvalidTaskStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Task management
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Create tasks for a workflow instance
     * 
     * Example curl command:
     * curl -X POST http://localhost:8080/api/tasks/workflow-instance/1
     */
    @PostMapping("/workflow-instance/{workflowInstanceId}")
    public ResponseEntity<List<TaskDto>> createTasksForWorkflowInstance(@PathVariable Long workflowInstanceId) {
        try {
            logger.info("Creating tasks for workflow instance: {}", workflowInstanceId);
            List<TaskDto> tasks = taskService.createTasksForWorkflowInstance(workflowInstanceId);
            return ResponseEntity.status(HttpStatus.CREATED).body(tasks);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error creating tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get task by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        try {
            TaskDto task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (ResourceNotFoundException e) {
            logger.error("Task not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get tasks by workflow instance ID
     */
    @GetMapping("/workflow-instance/{workflowInstanceId}")
    public ResponseEntity<List<TaskDto>> getTasksByWorkflowInstanceId(@PathVariable Long workflowInstanceId) {
        try {
            List<TaskDto> tasks = taskService.getTasksByWorkflowInstanceId(workflowInstanceId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching tasks by workflow instance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get tasks assigned to a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskDto>> getTasksByUserId(@PathVariable Long userId) {
        try {
            List<TaskDto> tasks = taskService.getTasksByUserId(userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching tasks by user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get pending tasks for a user
     */
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<TaskDto>> getPendingTasksForUser(@PathVariable Long userId) {
        try {
            List<TaskDto> tasks = taskService.getPendingTasksForUser(userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching pending tasks for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get completed tasks for a user
     */
    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<TaskDto>> getCompletedTasksForUser(@PathVariable Long userId) {
        try {
            List<TaskDto> tasks = taskService.getCompletedTasksForUser(userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching completed tasks for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get tasks by role
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<TaskDto>> getTasksByRole(@PathVariable Long roleId) {
        try {
            List<TaskDto> tasks = taskService.getTasksByRole(roleId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching tasks by role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update task status
     * 
     * Example curl command:
     * curl -X PUT http://localhost:8080/api/tasks/1/status \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "status": "COMPLETED",
     *     "comments": "Task completed successfully"
     *   }'
     */
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(@PathVariable Long taskId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String comments = request.get("comments");
            
            logger.info("Updating task {} status to: {}", taskId, status);
            TaskDto task = taskService.updateTaskStatus(taskId, status, comments);
            return ResponseEntity.ok(task);
        } catch (ResourceNotFoundException e) {
            logger.error("Task not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidTaskStateException e) {
            logger.error("Invalid task state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating task status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get ready tasks (tasks that can be started)
     */
    @GetMapping("/workflow-instance/{workflowInstanceId}/ready")
    public ResponseEntity<List<TaskDto>> getReadyTasks(@PathVariable Long workflowInstanceId) {
        try {
            List<TaskDto> tasks = taskService.getReadyTasks(workflowInstanceId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching ready tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get blocked tasks (tasks waiting for dependencies)
     */
    @GetMapping("/workflow-instance/{workflowInstanceId}/blocked")
    public ResponseEntity<List<TaskDto>> getBlockedTasks(@PathVariable Long workflowInstanceId) {
        try {
            List<TaskDto> tasks = taskService.getBlockedTasks(workflowInstanceId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error fetching blocked tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
