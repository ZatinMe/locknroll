package com.locknroll.repository;

import com.locknroll.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Workflow entity
 */
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    
    /**
     * Find workflow by name
     */
    Optional<Workflow> findByName(String name);
    
    /**
     * Find workflows by entity type
     */
    List<Workflow> findByEntityType(String entityType);
    
    /**
     * Find active workflows
     */
    List<Workflow> findByIsActiveTrue();
    
    /**
     * Find active workflows by entity type
     */
    List<Workflow> findByEntityTypeAndIsActiveTrue(String entityType);
    
    /**
     * Check if workflow name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find workflows with their steps
     */
    @Query("SELECT w FROM Workflow w LEFT JOIN FETCH w.steps WHERE w.id = :id")
    Optional<Workflow> findByIdWithSteps(@Param("id") Long id);
    
    /**
     * Find active workflows with their steps
     */
    @Query("SELECT w FROM Workflow w LEFT JOIN FETCH w.steps WHERE w.isActive = true")
    List<Workflow> findActiveWorkflowsWithSteps();
}
