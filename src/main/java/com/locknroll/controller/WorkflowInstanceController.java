package com.locknroll.controller;

import com.locknroll.dto.WorkflowInstanceDto;
import com.locknroll.service.WorkflowInstanceService;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.WorkflowInstanceAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for WorkflowInstance management
 */
@RestController
@RequestMapping("/api/workflow-instances")
@CrossOrigin(origins = "*")
public class WorkflowInstanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceController.class);
    
    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    
    /**
     * Create a new workflow instance
     * 
     * Example curl command:
     * curl -X POST http://localhost:8080/api/workflow-instances \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "workflowId": 1,
     *     "entityType": "FRUIT",
     *     "entityId": 1,
     *     "status": "PENDING"
     *   }'
     */
    @PostMapping
    public ResponseEntity<WorkflowInstanceDto> createWorkflowInstance(@Valid @RequestBody WorkflowInstanceDto workflowInstanceDto) {
        try {
            logger.info("Creating workflow instance for entity: {} {}", 
                       workflowInstanceDto.getEntityType(), workflowInstanceDto.getEntityId());
            WorkflowInstanceDto createdInstance = workflowInstanceService.createWorkflowInstance(workflowInstanceDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInstance);
        } catch (WorkflowInstanceAlreadyExistsException e) {
            logger.error("Workflow instance already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error creating workflow instance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get workflow instance by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowInstanceDto> getWorkflowInstanceById(@PathVariable Long id) {
        try {
            WorkflowInstanceDto workflowInstance = workflowInstanceService.getWorkflowInstanceById(id);
            return ResponseEntity.ok(workflowInstance);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching workflow instance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get workflow instance by entity type and entity ID
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<WorkflowInstanceDto> getWorkflowInstanceByEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        try {
            WorkflowInstanceDto workflowInstance = workflowInstanceService.getWorkflowInstanceByEntity(entityType, entityId);
            return ResponseEntity.ok(workflowInstance);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching workflow instance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all workflow instances
     */
    @GetMapping
    public ResponseEntity<List<WorkflowInstanceDto>> getAllWorkflowInstances() {
        try {
            List<WorkflowInstanceDto> instances = workflowInstanceService.getAllWorkflowInstances();
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            logger.error("Error fetching workflow instances: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get active workflow instances
     */
    @GetMapping("/active")
    public ResponseEntity<List<WorkflowInstanceDto>> getActiveWorkflowInstances() {
        try {
            List<WorkflowInstanceDto> instances = workflowInstanceService.getActiveWorkflowInstances();
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            logger.error("Error fetching active workflow instances: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get completed workflow instances
     */
    @GetMapping("/completed")
    public ResponseEntity<List<WorkflowInstanceDto>> getCompletedWorkflowInstances() {
        try {
            List<WorkflowInstanceDto> instances = workflowInstanceService.getCompletedWorkflowInstances();
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            logger.error("Error fetching completed workflow instances: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get workflow instances by entity type
     */
    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<List<WorkflowInstanceDto>> getWorkflowInstancesByEntityType(@PathVariable String entityType) {
        try {
            List<WorkflowInstanceDto> instances = workflowInstanceService.getWorkflowInstancesByEntityType(entityType);
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            logger.error("Error fetching workflow instances by entity type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get workflow instances by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<WorkflowInstanceDto>> getWorkflowInstancesByStatus(@PathVariable String status) {
        try {
            List<WorkflowInstanceDto> instances = workflowInstanceService.getWorkflowInstancesByStatus(status);
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            logger.error("Error fetching workflow instances by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update workflow instance status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<WorkflowInstanceDto> updateWorkflowInstanceStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            logger.info("Updating workflow instance {} status to: {}", id, status);
            WorkflowInstanceDto workflowInstance = workflowInstanceService.updateWorkflowInstanceStatus(id, status);
            return ResponseEntity.ok(workflowInstance);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating workflow instance status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Start workflow instance
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<WorkflowInstanceDto> startWorkflowInstance(@PathVariable Long id) {
        try {
            logger.info("Starting workflow instance: {}", id);
            WorkflowInstanceDto workflowInstance = workflowInstanceService.startWorkflowInstance(id);
            return ResponseEntity.ok(workflowInstance);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("Invalid workflow instance state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error starting workflow instance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Cancel workflow instance
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<WorkflowInstanceDto> cancelWorkflowInstance(@PathVariable Long id) {
        try {
            logger.info("Cancelling workflow instance: {}", id);
            WorkflowInstanceDto workflowInstance = workflowInstanceService.cancelWorkflowInstance(id);
            return ResponseEntity.ok(workflowInstance);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("Invalid workflow instance state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error cancelling workflow instance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
