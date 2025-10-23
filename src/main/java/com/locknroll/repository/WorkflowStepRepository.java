package com.locknroll.repository;

import com.locknroll.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for WorkflowStep entity
 */
@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {
    
    /**
     * Find steps by workflow ID
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflow.id = :workflowId ORDER BY ws.stepOrder")
    List<WorkflowStep> findByWorkflowIdOrderByStepOrder(@Param("workflowId") Long workflowId);
    
    /**
     * Find step by workflow ID and step order
     */
    Optional<WorkflowStep> findByWorkflowIdAndStepOrder(Long workflowId, Integer stepOrder);
    
    /**
     * Find steps by workflow ID and assigned role
     */
    List<WorkflowStep> findByWorkflowIdAndAssignedRoleId(Long workflowId, Long roleId);
    
    /**
     * Find steps with dependencies
     */
    // @Query("SELECT ws FROM WorkflowStep ws LEFT JOIN FETCH ws.dependencies WHERE ws.workflow.id = :workflowId ORDER BY ws.stepOrder")
    // List<WorkflowStep> findByWorkflowIdWithDependencies(@Param("workflowId") Long workflowId);
    
    /**
     * Count steps by workflow ID
     */
    long countByWorkflowId(Long workflowId);
    
    /**
     * Find next step in workflow
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflow.id = :workflowId AND ws.stepOrder > :currentStepOrder ORDER BY ws.stepOrder ASC")
    List<WorkflowStep> findNextSteps(@Param("workflowId") Long workflowId, @Param("currentStepOrder") Integer currentStepOrder);
    
    /**
     * Find previous steps in workflow
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflow.id = :workflowId AND ws.stepOrder < :currentStepOrder ORDER BY ws.stepOrder DESC")
    List<WorkflowStep> findPreviousSteps(@Param("workflowId") Long workflowId, @Param("currentStepOrder") Integer currentStepOrder);
}
