package com.locknroll.repository;

import com.locknroll.entity.WorkflowCondition;
import com.locknroll.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for WorkflowCondition entity
 */
@Repository
public interface WorkflowConditionRepository extends JpaRepository<WorkflowCondition, Long> {
    
    List<WorkflowCondition> findByWorkflowStep(WorkflowStep workflowStep);
    
    List<WorkflowCondition> findByWorkflowStepAndIsActive(WorkflowStep workflowStep, Boolean isActive);
    
    @Query("SELECT wc FROM WorkflowCondition wc WHERE wc.workflowStep.id = :stepId AND wc.isActive = true ORDER BY wc.priority DESC")
    List<WorkflowCondition> findActiveConditionsByStepIdOrderByPriority(@Param("stepId") Long stepId);
    
    @Query("SELECT wc FROM WorkflowCondition wc WHERE wc.workflowStep.workflow.id = :workflowId AND wc.isActive = true")
    List<WorkflowCondition> findActiveConditionsByWorkflowId(@Param("workflowId") Long workflowId);
}
