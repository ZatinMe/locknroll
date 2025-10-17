package com.locknroll.controller;

import com.locknroll.dto.WorkflowDto;
import com.locknroll.service.WorkflowService;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.WorkflowAlreadyExistsException;
import com.locknroll.exception.InvalidWorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for Workflow management
 */
@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")
public class WorkflowController {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);
    
    @Autowired
    private WorkflowService workflowService;
    
    /**
     * Create a new workflow
     * 
     * Example curl command:
     * curl -X POST http://localhost:8080/api/workflows \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "name": "Fruit Approval Workflow",
     *     "description": "Standard approval workflow for new fruits",
     *     "entityType": "FRUIT",
     *     "isActive": true,
     *     "steps": [
     *       {
     *         "stepOrder": 1,
     *         "stepName": "Finance Review",
     *         "stepType": "APPROVAL",
     *         "assignedRoleId": 1,
     *         "isRequired": true
     *       },
     *       {
     *         "stepOrder": 2,
     *         "stepName": "Quality Check",
     *         "stepType": "APPROVAL", 
     *         "assignedRoleId": 2,
     *         "isRequired": true
     *       },
     *       {
     *         "stepOrder": 3,
     *         "stepName": "Manager Approval",
     *         "stepType": "APPROVAL",
     *         "assignedRoleId": 3,
     *         "isRequired": true
     *       }
     *     ]
     *   }'
     */
    @PostMapping
    public ResponseEntity<WorkflowDto> createWorkflow(@Valid @RequestBody WorkflowDto workflowDto) {
        try {
            logger.info("Creating workflow: {}", workflowDto.getName());
            WorkflowDto createdWorkflow = workflowService.createWorkflow(workflowDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkflow);
        } catch (WorkflowAlreadyExistsException e) {
            logger.error("Workflow already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error creating workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get workflow by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowDto> getWorkflowById(@PathVariable Long id) {
        try {
            WorkflowDto workflow = workflowService.getWorkflowById(id);
            return ResponseEntity.ok(workflow);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get workflow by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<WorkflowDto> getWorkflowByName(@PathVariable String name) {
        try {
            WorkflowDto workflow = workflowService.getWorkflowByName(name);
            return ResponseEntity.ok(workflow);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all workflows
     */
    @GetMapping
    public ResponseEntity<List<WorkflowDto>> getAllWorkflows() {
        try {
            List<WorkflowDto> workflows = workflowService.getAllWorkflows();
            return ResponseEntity.ok(workflows);
        } catch (Exception e) {
            logger.error("Error fetching workflows: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get active workflows
     */
    @GetMapping("/active")
    public ResponseEntity<List<WorkflowDto>> getActiveWorkflows() {
        try {
            List<WorkflowDto> workflows = workflowService.getActiveWorkflows();
            return ResponseEntity.ok(workflows);
        } catch (Exception e) {
            logger.error("Error fetching active workflows: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get workflows by entity type
     */
    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<List<WorkflowDto>> getWorkflowsByEntityType(@PathVariable String entityType) {
        try {
            List<WorkflowDto> workflows = workflowService.getWorkflowsByEntityType(entityType);
            return ResponseEntity.ok(workflows);
        } catch (Exception e) {
            logger.error("Error fetching workflows by entity type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update workflow
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable Long id, @Valid @RequestBody WorkflowDto workflowDto) {
        try {
            WorkflowDto updatedWorkflow = workflowService.updateWorkflow(id, workflowDto);
            return ResponseEntity.ok(updatedWorkflow);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete workflow
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        try {
            workflowService.deleteWorkflow(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Activate workflow
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<WorkflowDto> activateWorkflow(@PathVariable Long id) {
        try {
            WorkflowDto activatedWorkflow = workflowService.activateWorkflow(id);
            return ResponseEntity.ok(activatedWorkflow);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error activating workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Deactivate workflow
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<WorkflowDto> deactivateWorkflow(@PathVariable Long id) {
        try {
            WorkflowDto deactivatedWorkflow = workflowService.deactivateWorkflow(id);
            return ResponseEntity.ok(deactivatedWorkflow);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deactivating workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Validate workflow configuration
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<String> validateWorkflow(@PathVariable Long id) {
        try {
            workflowService.validateWorkflow(id);
            return ResponseEntity.ok("Workflow configuration is valid");
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidWorkflowException e) {
            logger.error("Invalid workflow configuration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error validating workflow: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
