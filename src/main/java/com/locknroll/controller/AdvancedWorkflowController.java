package com.locknroll.controller;

import com.locknroll.dto.WorkflowInstanceDto;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.WorkflowException;
import com.locknroll.service.AdvancedWorkflowEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for advanced workflow features
 */
@RestController
@RequestMapping("/api/advanced-workflow")
public class AdvancedWorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedWorkflowController.class);

    @Autowired
    private AdvancedWorkflowEngine advancedWorkflowEngine;

    /**
     * Start an advanced workflow with conditional logic and parallel processing
     */
    @PostMapping("/start")
    public ResponseEntity<?> startAdvancedWorkflow(@RequestBody Map<String, Object> request) {
        try {
            String entityType = (String) request.get("entityType");
            String entityId = (String) request.get("entityId");
            String workflowName = (String) request.get("workflowName");
            Map<String, Object> context = (Map<String, Object>) request.get("context");

            if (entityType == null || entityId == null || workflowName == null) {
                return ResponseEntity.badRequest().body("Missing required parameters: entityType, entityId, workflowName");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String startedBy = authentication != null ? authentication.getName() : "system";

            WorkflowInstanceDto workflowInstance = advancedWorkflowEngine.startAdvancedWorkflow(
                    entityType, entityId, workflowName, startedBy, context != null ? context : Map.of());

            return ResponseEntity.status(HttpStatus.CREATED).body(workflowInstance);
        } catch (ResourceNotFoundException e) {
            logger.error("Error starting advanced workflow: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (WorkflowException e) {
            logger.error("Workflow execution error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while starting advanced workflow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Start a fruit approval workflow with advanced features
     */
    @PostMapping("/start-fruit-approval")
    public ResponseEntity<?> startFruitApprovalWorkflow(@RequestBody Map<String, Object> request) {
        try {
            String fruitId = (String) request.get("fruitId");
            String fruitName = (String) request.get("fruitName");
            String fruitType = (String) request.get("fruitType");
            String sellerId = (String) request.get("sellerId");

            if (fruitId == null) {
                return ResponseEntity.badRequest().body("Missing required parameter: fruitId");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String startedBy = authentication != null ? authentication.getName() : "system";

            // Create context for conditional logic
            Map<String, Object> context = Map.of(
                    "entityType", "FRUIT",
                    "fruitId", fruitId,
                    "fruitName", fruitName != null ? fruitName : "Unknown Fruit",
                    "fruitType", fruitType != null ? fruitType : "UNKNOWN",
                    "sellerId", sellerId != null ? sellerId : "unknown",
                    "userRole", "SELLER"
            );

            WorkflowInstanceDto workflowInstance = advancedWorkflowEngine.startAdvancedWorkflow(
                    "FRUIT", fruitId, "FRUIT_APPROVAL_WORKFLOW", startedBy, context);

            return ResponseEntity.status(HttpStatus.CREATED).body(workflowInstance);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while starting fruit approval workflow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Test conditional workflow with different contexts
     */
    @PostMapping("/test-conditional")
    public ResponseEntity<?> testConditionalWorkflow(@RequestBody Map<String, Object> request) {
        try {
            String entityType = (String) request.get("entityType");
            String entityId = (String) request.get("entityId");
            String workflowName = (String) request.get("workflowName");
            Map<String, Object> context = (Map<String, Object>) request.get("context");

            if (entityType == null || entityId == null || workflowName == null) {
                return ResponseEntity.badRequest().body("Missing required parameters: entityType, entityId, workflowName");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String startedBy = authentication != null ? authentication.getName() : "system";

            // Add test context
            if (context == null) {
                context = Map.of();
            }
            context.put("testMode", true);
            context.put("timestamp", System.currentTimeMillis());

            WorkflowInstanceDto workflowInstance = advancedWorkflowEngine.startAdvancedWorkflow(
                    entityType, entityId, workflowName, startedBy, context);

            return ResponseEntity.status(HttpStatus.CREATED).body(workflowInstance);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while testing conditional workflow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Get workflow execution status with advanced features
     */
    @GetMapping("/status/{workflowInstanceId}")
    public ResponseEntity<?> getAdvancedWorkflowStatus(@PathVariable Long workflowInstanceId) {
        try {
            // This would return enhanced status information including parallel processing status
            // For now, return a simple response
            return ResponseEntity.ok(Map.of(
                    "workflowInstanceId", workflowInstanceId,
                    "status", "IN_PROGRESS",
                    "features", Map.of(
                            "conditionalApprovals", true,
                            "parallelProcessing", true,
                            "timeoutHandling", true
                    ),
                    "message", "Advanced workflow features are active"
            ));
        } catch (Exception e) {
            logger.error("An unexpected error occurred while getting advanced workflow status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
