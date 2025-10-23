package com.locknroll.repository;

import com.locknroll.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for WorkflowInstance entity
 */
@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    
    /**
     * Find workflow instances by entity type and entity ID
     */
    Optional<WorkflowInstance> findByEntityTypeAndEntityId(String entityType, String entityId);
    
    /**
     * Find workflow instances by workflow ID
     */
    List<WorkflowInstance> findByWorkflowIdOrderByCreatedAt(Long workflowId);
    
    /**
     * Find workflow instances by status
     */
    List<WorkflowInstance> findByStatusOrderByCreatedAt(String status);
    
    /**
     * Find workflow instances by entity type
     */
    List<WorkflowInstance> findByEntityTypeOrderByCreatedAt(String entityType);
    
    /**
     * Find active workflow instances
     */
    @Query("SELECT wi FROM WorkflowInstance wi WHERE wi.status IN ('PENDING', 'IN_PROGRESS') ORDER BY wi.createdAt DESC")
    List<WorkflowInstance> findActiveWorkflowInstances();
    
    /**
     * Find completed workflow instances
     */
    @Query("SELECT wi FROM WorkflowInstance wi WHERE wi.status IN ('COMPLETED', 'REJECTED', 'CANCELLED') ORDER BY wi.completedAt DESC")
    List<WorkflowInstance> findCompletedWorkflowInstances();
    
    /**
     * Count active workflow instances
     */
    @Query("SELECT COUNT(wi) FROM WorkflowInstance wi WHERE wi.status IN ('PENDING', 'IN_PROGRESS')")
    long countActiveWorkflowInstances();
    
    /**
     * Find workflow instances with their tasks
     */
    @Query("SELECT wi FROM WorkflowInstance wi LEFT JOIN FETCH wi.tasks WHERE wi.id = :id")
    Optional<WorkflowInstance> findByIdWithTasks(@Param("id") Long id);
    
    /**
     * Find workflow instances by entity type and status
     */
    List<WorkflowInstance> findByEntityTypeAndStatusOrderByCreatedAt(String entityType, String status);
    
    /**
     * Find workflow instances by workflow ID and status
     */
    List<WorkflowInstance> findByWorkflowIdAndStatus(Long workflowId, String status);
}
