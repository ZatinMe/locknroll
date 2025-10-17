package com.locknroll.controller;

import com.locknroll.dto.WorkflowInstanceDto;
import com.locknroll.dto.TaskDto;
import com.locknroll.entity.Fruit;
import com.locknroll.service.FruitWorkflowService;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.InvalidFruitStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Fruit workflow operations
 */
@RestController
@RequestMapping("/api/fruits/workflow")
@CrossOrigin(origins = "*")
public class FruitWorkflowController {
    
    private static final Logger logger = LoggerFactory.getLogger(FruitWorkflowController.class);
    
    @Autowired
    private FruitWorkflowService fruitWorkflowService;
    
    /**
     * Submit a fruit for approval
     * 
     * Example curl command:
     * curl -X POST http://localhost:8080/api/fruits/workflow/1/submit \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "submittedBy": "seller1",
     *     "comments": "Ready for approval"
     *   }'
     */
    @PostMapping("/{fruitId}/submit")
    public ResponseEntity<WorkflowInstanceDto> submitFruitForApproval(@PathVariable Long fruitId, @RequestBody Map<String, String> request) {
        try {
            String submittedBy = request.get("submittedBy");
            logger.info("Submitting fruit {} for approval by {}", fruitId, submittedBy);
            
            WorkflowInstanceDto workflowInstance = fruitWorkflowService.submitFruitForApproval(fruitId, submittedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(workflowInstance);
        } catch (ResourceNotFoundException e) {
            logger.error("Fruit not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidFruitStateException e) {
            logger.error("Invalid fruit state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error submitting fruit for approval: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Approve a fruit
     * 
     * Example curl command:
     * curl -X POST http://localhost:8080/api/fruits/workflow/1/approve \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "approvedBy": "manager1",
     *     "comments": "Approved after review"
     *   }'
     */
    @PostMapping("/{fruitId}/approve")
    public ResponseEntity<Fruit> approveFruit(@PathVariable Long fruitId, @RequestBody Map<String, String> request) {
        try {
            String approvedBy = request.get("approvedBy");
            String comments = request.get("comments");
            
            logger.info("Approving fruit {} by {}", fruitId, approvedBy);
            Fruit approvedFruit = fruitWorkflowService.approveFruit(fruitId, approvedBy, comments);
            return ResponseEntity.ok(approvedFruit);
        } catch (ResourceNotFoundException e) {
            logger.error("Fruit not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidFruitStateException e) {
            logger.error("Invalid fruit state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error approving fruit: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Reject a fruit
     * 
     * Example curl command:
     * curl -X POST http://localhost:8080/api/fruits/workflow/1/reject \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "rejectedBy": "manager1",
     *     "rejectionReason": "Quality issues found"
     *   }'
     */
    @PostMapping("/{fruitId}/reject")
    public ResponseEntity<Fruit> rejectFruit(@PathVariable Long fruitId, @RequestBody Map<String, String> request) {
        try {
            String rejectedBy = request.get("rejectedBy");
            String rejectionReason = request.get("rejectionReason");
            
            logger.info("Rejecting fruit {} by {} with reason: {}", fruitId, rejectedBy, rejectionReason);
            Fruit rejectedFruit = fruitWorkflowService.rejectFruit(fruitId, rejectedBy, rejectionReason);
            return ResponseEntity.ok(rejectedFruit);
        } catch (ResourceNotFoundException e) {
            logger.error("Fruit not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InvalidFruitStateException e) {
            logger.error("Invalid fruit state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error rejecting fruit: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get fruits by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Fruit>> getFruitsByStatus(@PathVariable String status) {
        try {
            List<Fruit> fruits = fruitWorkflowService.getFruitsByStatus(status);
            return ResponseEntity.ok(fruits);
        } catch (Exception e) {
            logger.error("Error fetching fruits by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get fruits pending approval
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Fruit>> getFruitsPendingApproval() {
        try {
            List<Fruit> fruits = fruitWorkflowService.getFruitsPendingApproval();
            return ResponseEntity.ok(fruits);
        } catch (Exception e) {
            logger.error("Error fetching fruits pending approval: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get approved fruits
     */
    @GetMapping("/approved")
    public ResponseEntity<List<Fruit>> getApprovedFruits() {
        try {
            List<Fruit> fruits = fruitWorkflowService.getApprovedFruits();
            return ResponseEntity.ok(fruits);
        } catch (Exception e) {
            logger.error("Error fetching approved fruits: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get rejected fruits
     */
    @GetMapping("/rejected")
    public ResponseEntity<List<Fruit>> getRejectedFruits() {
        try {
            List<Fruit> fruits = fruitWorkflowService.getRejectedFruits();
            return ResponseEntity.ok(fruits);
        } catch (Exception e) {
            logger.error("Error fetching rejected fruits: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get draft fruits
     */
    @GetMapping("/draft")
    public ResponseEntity<List<Fruit>> getDraftFruits() {
        try {
            List<Fruit> fruits = fruitWorkflowService.getDraftFruits();
            return ResponseEntity.ok(fruits);
        } catch (Exception e) {
            logger.error("Error fetching draft fruits: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get workflow instance for a fruit
     */
    @GetMapping("/{fruitId}/workflow-instance")
    public ResponseEntity<WorkflowInstanceDto> getFruitWorkflowInstance(@PathVariable Long fruitId) {
        try {
            WorkflowInstanceDto workflowInstance = fruitWorkflowService.getFruitWorkflowInstance(fruitId);
            return ResponseEntity.ok(workflowInstance);
        } catch (ResourceNotFoundException e) {
            logger.error("Workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching fruit workflow instance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get tasks for a fruit's approval workflow
     */
    @GetMapping("/{fruitId}/tasks")
    public ResponseEntity<List<TaskDto>> getFruitApprovalTasks(@PathVariable Long fruitId) {
        try {
            List<TaskDto> tasks = fruitWorkflowService.getFruitApprovalTasks(fruitId);
            return ResponseEntity.ok(tasks);
        } catch (ResourceNotFoundException e) {
            logger.error("Fruit or workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching fruit approval tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get pending tasks for a fruit's approval workflow
     */
    @GetMapping("/{fruitId}/tasks/pending")
    public ResponseEntity<List<TaskDto>> getFruitPendingTasks(@PathVariable Long fruitId) {
        try {
            List<TaskDto> tasks = fruitWorkflowService.getFruitPendingTasks(fruitId);
            return ResponseEntity.ok(tasks);
        } catch (ResourceNotFoundException e) {
            logger.error("Fruit or workflow instance not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching fruit pending tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Check if a fruit can be submitted for approval
     */
    @GetMapping("/{fruitId}/can-submit")
    public ResponseEntity<Map<String, Boolean>> canSubmitFruitForApproval(@PathVariable Long fruitId) {
        try {
            boolean canSubmit = fruitWorkflowService.canSubmitFruitForApproval(fruitId);
            return ResponseEntity.ok(Map.of("canSubmit", canSubmit));
        } catch (ResourceNotFoundException e) {
            logger.error("Fruit not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error checking if fruit can be submitted: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Check if a fruit is in approval process
     */
    @GetMapping("/{fruitId}/in-approval")
    public ResponseEntity<Map<String, Boolean>> isFruitInApprovalProcess(@PathVariable Long fruitId) {
        try {
            boolean inApproval = fruitWorkflowService.isFruitInApprovalProcess(fruitId);
            return ResponseEntity.ok(Map.of("inApproval", inApproval));
        } catch (ResourceNotFoundException e) {
            logger.error("Fruit not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error checking if fruit is in approval process: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get fruits by user (for seller dashboard)
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Fruit>> getFruitsByUser(@PathVariable String username) {
        try {
            List<Fruit> fruits = fruitWorkflowService.getFruitsByUser(username);
            return ResponseEntity.ok(fruits);
        } catch (Exception e) {
            logger.error("Error fetching fruits by user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get fruits by user and status
     */
    @GetMapping("/user/{username}/status/{status}")
    public ResponseEntity<List<Fruit>> getFruitsByUserAndStatus(@PathVariable String username, @PathVariable String status) {
        try {
            List<Fruit> fruits = fruitWorkflowService.getFruitsByUserAndStatus(username, status);
            return ResponseEntity.ok(fruits);
        } catch (Exception e) {
            logger.error("Error fetching fruits by user and status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
