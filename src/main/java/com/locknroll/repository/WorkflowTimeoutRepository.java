package com.locknroll.repository;

import com.locknroll.entity.WorkflowTimeout;
import com.locknroll.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for WorkflowTimeout entity
 */
@Repository
public interface WorkflowTimeoutRepository extends JpaRepository<WorkflowTimeout, Long> {
    
    List<WorkflowTimeout> findByWorkflowStep(WorkflowStep workflowStep);
    
    List<WorkflowTimeout> findByWorkflowStepAndIsActive(WorkflowStep workflowStep, Boolean isActive);
    
    @Query("SELECT wt FROM WorkflowTimeout wt WHERE wt.isActive = true AND wt.lastChecked < :checkTime")
    List<WorkflowTimeout> findOverdueTimeouts(@Param("checkTime") LocalDateTime checkTime);
    
    @Query("SELECT wt FROM WorkflowTimeout wt WHERE wt.workflowStep.workflow.id = :workflowId AND wt.isActive = true")
    List<WorkflowTimeout> findActiveTimeoutsByWorkflowId(@Param("workflowId") Long workflowId);
}
