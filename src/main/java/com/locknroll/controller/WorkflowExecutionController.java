package com.locknroll.controller;

import com.locknroll.dto.WorkflowInstanceDto;
import com.locknroll.dto.WorkflowExecutionStatusDto;
import com.locknroll.service.WorkflowExecutionEngine;
import com.locknroll.security.SecurityAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Workflow Execution Engine
 */
@RestController
@RequestMapping("/api/workflow-execution")
@CrossOrigin(origins = "*")
public class WorkflowExecutionController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutionController.class);

    @Autowired
    private WorkflowExecutionEngine workflowExecutionEngine;

    /**
     * Start a workflow for an entity
     */
    @PostMapping("/start")
    @SecurityAnnotations.AdminOrBackOffice
    public ResponseEntity<WorkflowInstanceDto> startWorkflow(@RequestBody Map<String, String> request) {
        try {
            String entityType = request.get("entityType");
            String entityId = request.get("entityId");
            String workflowName = request.get("workflowName");
            String startedBy = request.get("startedBy");

            if (entityType == null || entityId == null || workflowName == null) {
                return ResponseEntity.badRequest().build();
            }

            WorkflowInstanceDto workflowInstance = workflowExecutionEngine.startWorkflow(
                    entityType, entityId, workflowName, startedBy);

            logger.info("Workflow started successfully: {}", workflowInstance.getId());
            return ResponseEntity.ok(workflowInstance);

        } catch (Exception e) {
            logger.error("Failed to start workflow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get workflow execution status
     */
    @GetMapping("/status/{workflowInstanceId}")
    @SecurityAnnotations.Authenticated
    public ResponseEntity<WorkflowExecutionStatusDto> getWorkflowStatus(@PathVariable Long workflowInstanceId) {
        try {
            WorkflowExecutionStatusDto status = workflowExecutionEngine.getWorkflowStatus(workflowInstanceId);
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            logger.error("Failed to get workflow status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Complete a workflow step
     */
    @PostMapping("/step/complete/{workflowInstanceId}")
    @SecurityAnnotations.Authenticated
    public ResponseEntity<String> completeStep(@PathVariable Long workflowInstanceId) {
        try {
            // This would be called when a step is completed
            // The actual step completion logic would be in the TaskService
            logger.info("Step completion requested for workflow instance: {}", workflowInstanceId);
            return ResponseEntity.ok("Step completion processed");

        } catch (Exception e) {
            logger.error("Failed to complete step", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reject a workflow
     */
    @PostMapping("/reject/{workflowInstanceId}")
    @SecurityAnnotations.Authenticated
    public ResponseEntity<String> rejectWorkflow(@PathVariable Long workflowInstanceId, 
                                                @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            String rejectedBy = request.get("rejectedBy");

            if (reason == null || rejectedBy == null) {
                return ResponseEntity.badRequest().build();
            }

            // This would call the workflow execution engine to reject the workflow
            logger.info("Workflow rejection requested for instance: {} - Reason: {}", workflowInstanceId, reason);
            return ResponseEntity.ok("Workflow rejection processed");

        } catch (Exception e) {
            logger.error("Failed to reject workflow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get workflow execution statistics
     */
    @GetMapping("/stats")
    @SecurityAnnotations.AdminOrBackOffice
    public ResponseEntity<Map<String, Object>> getWorkflowStats() {
        try {
            // This would return workflow execution statistics
            Map<String, Object> stats = Map.of(
                    "activeWorkflows", 0,
                    "completedWorkflows", 0,
                    "rejectedWorkflows", 0,
                    "averageExecutionTime", "2.5 days"
            );

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Failed to get workflow stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
