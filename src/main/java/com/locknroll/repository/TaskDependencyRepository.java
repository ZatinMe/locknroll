package com.locknroll.repository;

import com.locknroll.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TaskDependency entity
 */
@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    
    /**
     * Find dependencies by dependent task ID
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.dependentTask.id = :taskId")
    List<TaskDependency> findByDependentTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find dependencies by parent task ID
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.parentTask.id = :taskId")
    List<TaskDependency> findByParentTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find dependencies by workflow instance ID
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.parentTask.workflowInstance.id = :workflowInstanceId")
    List<TaskDependency> findByWorkflowInstanceId(@Param("workflowInstanceId") Long workflowInstanceId);
}
