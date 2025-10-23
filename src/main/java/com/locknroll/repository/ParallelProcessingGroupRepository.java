package com.locknroll.repository;

import com.locknroll.entity.ParallelProcessingGroup;
import com.locknroll.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ParallelProcessingGroup entity
 */
@Repository
public interface ParallelProcessingGroupRepository extends JpaRepository<ParallelProcessingGroup, Long> {
    
    List<ParallelProcessingGroup> findByWorkflow(Workflow workflow);
    
    List<ParallelProcessingGroup> findByWorkflowAndIsActive(Workflow workflow, Boolean isActive);
    
    @Query("SELECT ppg FROM ParallelProcessingGroup ppg WHERE ppg.workflow.id = :workflowId AND ppg.isActive = true ORDER BY ppg.groupOrder")
    List<ParallelProcessingGroup> findActiveGroupsByWorkflowIdOrderByGroupOrder(@Param("workflowId") Long workflowId);
    
    @Query("SELECT ppg FROM ParallelProcessingGroup ppg WHERE ppg.workflow.id = :workflowId AND ppg.groupOrder = :groupOrder AND ppg.isActive = true")
    List<ParallelProcessingGroup> findByWorkflowIdAndGroupOrder(@Param("workflowId") Long workflowId, @Param("groupOrder") Integer groupOrder);
}
