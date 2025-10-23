package com.locknroll.service;

import com.locknroll.dto.WorkflowInstanceDto;
import com.locknroll.dto.TaskDto;
import com.locknroll.entity.Fruit;
import com.locknroll.entity.WorkflowInstance;
import com.locknroll.entity.Workflow;
import com.locknroll.repository.FruitRepository;
import com.locknroll.repository.WorkflowRepository;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.InvalidFruitStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Fruit workflow operations
 */
@Service
@Transactional
public class FruitWorkflowService {
    
    private static final Logger logger = LoggerFactory.getLogger(FruitWorkflowService.class);
    
    @Autowired
    private FruitRepository fruitRepository;
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Submit a fruit for approval
     * This creates a workflow instance and tasks for the approval process
     */
    public WorkflowInstanceDto submitFruitForApproval(Long fruitId, String submittedBy) {
        logger.info("Submitting fruit {} for approval by {}", fruitId, submittedBy);
        
        // Get the fruit
        Fruit fruit = fruitRepository.findById(fruitId)
                .orElseThrow(() -> new ResourceNotFoundException("Fruit not found with id: " + fruitId));
        
        // Validate fruit state
        if (!"DRAFT".equals(fruit.getStatus())) {
            throw new InvalidFruitStateException("Fruit must be in DRAFT status to submit for approval. Current status: " + fruit.getStatus());
        }
        
        // Get the fruit approval workflow
        Workflow fruitWorkflow = workflowRepository.findByName("Fruit Approval Workflow")
                .orElseThrow(() -> new ResourceNotFoundException("Fruit Approval Workflow not found"));
        
        // Create workflow instance
        WorkflowInstanceDto workflowInstanceDto = new WorkflowInstanceDto();
        workflowInstanceDto.setWorkflowId(fruitWorkflow.getId());
        workflowInstanceDto.setEntityType("FRUIT");
        workflowInstanceDto.setEntityId(fruitId.toString());
        workflowInstanceDto.setStatus("PENDING");
        
        WorkflowInstanceDto createdInstance = workflowInstanceService.createWorkflowInstance(workflowInstanceDto);
        
        // Update fruit status
        fruit.setStatus("PENDING_APPROVAL");
        fruit.setSubmittedBy(submittedBy);
        fruit.setSubmittedAt(LocalDateTime.now());
        fruit.setUpdatedBy(submittedBy);
        
        fruitRepository.save(fruit);
        
        // Create tasks for the workflow instance
        List<TaskDto> tasks = taskService.createTasksForWorkflowInstance(createdInstance.getId());
        
        logger.info("Submitted fruit {} for approval. Created workflow instance {} with {} tasks", 
                   fruitId, createdInstance.getId(), tasks.size());
        
        return createdInstance;
    }
    
    /**
     * Approve a fruit
     * This completes the approval workflow and updates the fruit status
     */
    public Fruit approveFruit(Long fruitId, String approvedBy, String comments) {
        logger.info("Approving fruit {} by {}", fruitId, approvedBy);
        
        // Get the fruit
        Fruit fruit = fruitRepository.findById(fruitId)
                .orElseThrow(() -> new ResourceNotFoundException("Fruit not found with id: " + fruitId));
        
        // Validate fruit state
        if (!"PENDING_APPROVAL".equals(fruit.getStatus())) {
            throw new InvalidFruitStateException("Fruit must be in PENDING_APPROVAL status to approve. Current status: " + fruit.getStatus());
        }
        
        // Get the workflow instance for this fruit
        WorkflowInstanceDto workflowInstance = workflowInstanceService.getWorkflowInstanceByEntity("FRUIT", fruitId.toString());
        
        // Update workflow instance status to completed
        workflowInstanceService.updateWorkflowInstanceStatus(workflowInstance.getId(), "COMPLETED");
        
        // Update fruit status
        fruit.setStatus("APPROVED");
        fruit.setApprovedAt(LocalDateTime.now());
        fruit.setUpdatedBy(approvedBy);
        
        Fruit savedFruit = fruitRepository.save(fruit);
        
        logger.info("Approved fruit {} by {}", fruitId, approvedBy);
        return savedFruit;
    }
    
    /**
     * Reject a fruit
     * This completes the approval workflow and updates the fruit status
     */
    public Fruit rejectFruit(Long fruitId, String rejectedBy, String rejectionReason) {
        logger.info("Rejecting fruit {} by {} with reason: {}", fruitId, rejectedBy, rejectionReason);
        
        // Get the fruit
        Fruit fruit = fruitRepository.findById(fruitId)
                .orElseThrow(() -> new ResourceNotFoundException("Fruit not found with id: " + fruitId));
        
        // Validate fruit state
        if (!"PENDING_APPROVAL".equals(fruit.getStatus())) {
            throw new InvalidFruitStateException("Fruit must be in PENDING_APPROVAL status to reject. Current status: " + fruit.getStatus());
        }
        
        // Get the workflow instance for this fruit
        WorkflowInstanceDto workflowInstance = workflowInstanceService.getWorkflowInstanceByEntity("FRUIT", fruitId.toString());
        
        // Update workflow instance status to rejected
        workflowInstanceService.updateWorkflowInstanceStatus(workflowInstance.getId(), "REJECTED");
        
        // Update fruit status
        fruit.setStatus("REJECTED");
        fruit.setRejectedAt(LocalDateTime.now());
        fruit.setRejectionReason(rejectionReason);
        fruit.setUpdatedBy(rejectedBy);
        
        Fruit savedFruit = fruitRepository.save(fruit);
        
        logger.info("Rejected fruit {} by {} with reason: {}", fruitId, rejectedBy, rejectionReason);
        return savedFruit;
    }
    
    /**
     * Get fruits by status
     */
    @Transactional(readOnly = true)
    public List<Fruit> getFruitsByStatus(String status) {
        logger.debug("Fetching fruits with status: {}", status);
        return fruitRepository.findByStatus(status);
    }
    
    /**
     * Get fruits pending approval
     */
    @Transactional(readOnly = true)
    public List<Fruit> getFruitsPendingApproval() {
        return getFruitsByStatus("PENDING_APPROVAL");
    }
    
    /**
     * Get approved fruits
     */
    @Transactional(readOnly = true)
    public List<Fruit> getApprovedFruits() {
        return getFruitsByStatus("APPROVED");
    }
    
    /**
     * Get rejected fruits
     */
    @Transactional(readOnly = true)
    public List<Fruit> getRejectedFruits() {
        return getFruitsByStatus("REJECTED");
    }
    
    /**
     * Get draft fruits
     */
    @Transactional(readOnly = true)
    public List<Fruit> getDraftFruits() {
        return getFruitsByStatus("DRAFT");
    }
    
    /**
     * Get workflow instance for a fruit
     */
    @Transactional(readOnly = true)
    public WorkflowInstanceDto getFruitWorkflowInstance(Long fruitId) {
        return workflowInstanceService.getWorkflowInstanceByEntity("FRUIT", fruitId.toString());
    }
    
    /**
     * Get tasks for a fruit's approval workflow
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getFruitApprovalTasks(Long fruitId) {
        WorkflowInstanceDto workflowInstance = getFruitWorkflowInstance(fruitId);
        return taskService.getTasksByWorkflowInstanceId(workflowInstance.getId());
    }
    
    /**
     * Get pending tasks for a fruit's approval workflow
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getFruitPendingTasks(Long fruitId) {
        WorkflowInstanceDto workflowInstance = getFruitWorkflowInstance(fruitId);
        return taskService.getReadyTasks(workflowInstance.getId());
    }
    
    /**
     * Check if a fruit can be submitted for approval
     */
    @Transactional(readOnly = true)
    public boolean canSubmitFruitForApproval(Long fruitId) {
        Fruit fruit = fruitRepository.findById(fruitId)
                .orElseThrow(() -> new ResourceNotFoundException("Fruit not found with id: " + fruitId));
        
        return "DRAFT".equals(fruit.getStatus());
    }
    
    /**
     * Check if a fruit is in approval process
     */
    @Transactional(readOnly = true)
    public boolean isFruitInApprovalProcess(Long fruitId) {
        Fruit fruit = fruitRepository.findById(fruitId)
                .orElseThrow(() -> new ResourceNotFoundException("Fruit not found with id: " + fruitId));
        
        return "PENDING_APPROVAL".equals(fruit.getStatus());
    }
    
    /**
     * Get fruits by user (for seller dashboard)
     */
    @Transactional(readOnly = true)
    public List<Fruit> getFruitsByUser(String username) {
        logger.debug("Fetching fruits for user: {}", username);
        return fruitRepository.findByCreatedBy(username);
    }
    
    /**
     * Get fruits by user and status
     */
    @Transactional(readOnly = true)
    public List<Fruit> getFruitsByUserAndStatus(String username, String status) {
        logger.debug("Fetching fruits for user: {} with status: {}", username, status);
        return fruitRepository.findByCreatedByAndStatus(username, status);
    }
}
