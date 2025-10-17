package com.locknroll.repository;

import com.locknroll.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Task entity
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find tasks by workflow instance ID
     */
    List<Task> findByWorkflowInstanceIdOrderByCreatedAt(Long workflowInstanceId);
    
    /**
     * Find tasks by assigned user ID
     */
    List<Task> findByAssignedToIdOrderByCreatedAt(Long userId);
    
    /**
     * Find tasks by assigned user ID and status
     */
    List<Task> findByAssignedToIdAndStatusOrderByCreatedAt(Long userId, String status);
    
    /**
     * Find tasks by workflow instance ID and status
     */
    List<Task> findByWorkflowInstanceIdAndStatusOrderByCreatedAt(Long workflowInstanceId, String status);
    
    /**
     * Find tasks by workflow step ID
     */
    List<Task> findByWorkflowStepIdOrderByCreatedAt(Long workflowStepId);
    
    /**
     * Find tasks by status
     */
    List<Task> findByStatusOrderByCreatedAt(String status);
    
    /**
     * Find pending tasks for a user
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'PENDING' ORDER BY t.createdAt ASC")
    List<Task> findPendingTasksForUser(@Param("userId") Long userId);
    
    /**
     * Find completed tasks for a user
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'COMPLETED' ORDER BY t.completedAt DESC")
    List<Task> findCompletedTasksForUser(@Param("userId") Long userId);
    
    /**
     * Find tasks by role
     */
    @Query("SELECT t FROM Task t WHERE t.workflowStep.assignedRole.id = :roleId AND t.status = 'PENDING' ORDER BY t.createdAt ASC")
    List<Task> findPendingTasksByRole(@Param("roleId") Long roleId);
    
    /**
     * Count pending tasks for a user
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'PENDING'")
    long countPendingTasksForUser(@Param("userId") Long userId);
    
    /**
     * Find tasks by status
     */
    List<Task> findByStatus(String status);
    
    /**
     * Find tasks by assigned user ID
     */
    List<Task> findByAssignedToId(Long assignedToId);
    
    /**
     * Find tasks with dependencies
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.dependencies WHERE t.workflowInstance.id = :workflowInstanceId ORDER BY t.createdAt")
    List<Task> findByWorkflowInstanceIdWithDependencies(@Param("workflowInstanceId") Long workflowInstanceId);
    
    /**
     * Find tasks that are ready to be started (all dependencies completed)
     */
    @Query("SELECT t FROM Task t WHERE t.workflowInstance.id = :workflowInstanceId AND t.status = 'PENDING' AND " +
           "NOT EXISTS (SELECT 1 FROM TaskDependency td WHERE td.dependentTask.id = t.id AND td.parentTask.status != 'COMPLETED')")
    List<Task> findReadyTasks(@Param("workflowInstanceId") Long workflowInstanceId);
    
    /**
     * Find tasks that are blocked by dependencies
     */
    @Query("SELECT t FROM Task t WHERE t.workflowInstance.id = :workflowInstanceId AND t.status = 'PENDING' AND " +
           "EXISTS (SELECT 1 FROM TaskDependency td WHERE td.dependentTask.id = t.id AND td.parentTask.status != 'COMPLETED')")
    List<Task> findBlockedTasks(@Param("workflowInstanceId") Long workflowInstanceId);
}
