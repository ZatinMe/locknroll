package com.locknroll.service;

import com.locknroll.dto.WorkflowInstanceDto;
import com.locknroll.entity.WorkflowInstance;
import com.locknroll.entity.Workflow;
import com.locknroll.repository.WorkflowInstanceRepository;
import com.locknroll.repository.WorkflowRepository;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.WorkflowInstanceAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for WorkflowInstance operations
 */
@Service
@Transactional
public class WorkflowInstanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceService.class);
    
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    /**
     * Create a new workflow instance
     */
    public WorkflowInstanceDto createWorkflowInstance(WorkflowInstanceDto workflowInstanceDto) {
        logger.info("Creating workflow instance for entity: {} {}", 
                   workflowInstanceDto.getEntityType(), workflowInstanceDto.getEntityId());
        
        // Check if workflow instance already exists for this entity
        Optional<WorkflowInstance> existingInstance = workflowInstanceRepository
                .findByEntityTypeAndEntityId(workflowInstanceDto.getEntityType(), workflowInstanceDto.getEntityId());
        
        if (existingInstance.isPresent()) {
            throw new WorkflowInstanceAlreadyExistsException(
                    "Workflow instance already exists for entity: " + workflowInstanceDto.getEntityType() + 
                    " with ID: " + workflowInstanceDto.getEntityId());
        }
        
        // Get workflow
        Workflow workflow = workflowRepository.findById(workflowInstanceDto.getWorkflowId())
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowInstanceDto.getWorkflowId()));
        
        // Create workflow instance
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflow(workflow);
        workflowInstance.setEntityType(workflowInstanceDto.getEntityType());
        workflowInstance.setEntityId(workflowInstanceDto.getEntityId());
        workflowInstance.setStatus("PENDING");
        workflowInstance.setCreatedBy("system"); // TODO: Get from security context
        
        WorkflowInstance savedInstance = workflowInstanceRepository.save(workflowInstance);
        
        logger.info("Created workflow instance: {} for entity: {} {}", 
                   savedInstance.getId(), savedInstance.getEntityType(), savedInstance.getEntityId());
        
        return convertToDto(savedInstance);
    }
    
    /**
     * Get workflow instance by ID
     */
    @Transactional(readOnly = true)
    public WorkflowInstanceDto getWorkflowInstanceById(Long id) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow instance not found with id: " + id));
        
        return convertToDto(workflowInstance);
    }
    
    /**
     * Get workflow instance by entity type and entity ID
     */
    @Transactional(readOnly = true)
    public WorkflowInstanceDto getWorkflowInstanceByEntity(String entityType, String entityId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository
                .findByEntityTypeAndEntityId(entityType, entityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow instance not found for entity: " + entityType + " with ID: " + entityId));
        
        return convertToDto(workflowInstance);
    }
    
    /**
     * Get all workflow instances
     */
    @Transactional(readOnly = true)
    public List<WorkflowInstanceDto> getAllWorkflowInstances() {
        List<WorkflowInstance> instances = workflowInstanceRepository.findAll();
        return instances.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active workflow instances
     */
    @Transactional(readOnly = true)
    public List<WorkflowInstanceDto> getActiveWorkflowInstances() {
        List<WorkflowInstance> instances = workflowInstanceRepository.findActiveWorkflowInstances();
        return instances.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get completed workflow instances
     */
    @Transactional(readOnly = true)
    public List<WorkflowInstanceDto> getCompletedWorkflowInstances() {
        List<WorkflowInstance> instances = workflowInstanceRepository.findCompletedWorkflowInstances();
        return instances.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get workflow instances by entity type
     */
    @Transactional(readOnly = true)
    public List<WorkflowInstanceDto> getWorkflowInstancesByEntityType(String entityType) {
        List<WorkflowInstance> instances = workflowInstanceRepository.findByEntityTypeOrderByCreatedAt(entityType);
        return instances.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get workflow instances by status
     */
    @Transactional(readOnly = true)
    public List<WorkflowInstanceDto> getWorkflowInstancesByStatus(String status) {
        List<WorkflowInstance> instances = workflowInstanceRepository.findByStatusOrderByCreatedAt(status);
        return instances.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Update workflow instance status
     */
    public WorkflowInstanceDto updateWorkflowInstanceStatus(Long id, String status) {
        logger.info("Updating workflow instance {} status to: {}", id, status);
        
        WorkflowInstance workflowInstance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow instance not found with id: " + id));
        
        workflowInstance.setStatus(status);
        workflowInstance.setUpdatedBy("system"); // TODO: Get from security context
        
        if ("COMPLETED".equals(status) || "REJECTED".equals(status) || "CANCELLED".equals(status)) {
            workflowInstance.setCompletedAt(LocalDateTime.now());
        }
        
        WorkflowInstance savedInstance = workflowInstanceRepository.save(workflowInstance);
        
        logger.info("Updated workflow instance {} status to: {}", id, status);
        return convertToDto(savedInstance);
    }
    
    /**
     * Start workflow instance
     */
    public WorkflowInstanceDto startWorkflowInstance(Long id) {
        logger.info("Starting workflow instance: {}", id);
        
        WorkflowInstance workflowInstance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow instance not found with id: " + id));
        
        if (!"PENDING".equals(workflowInstance.getStatus())) {
            throw new IllegalStateException("Cannot start workflow instance with status: " + workflowInstance.getStatus());
        }
        
        workflowInstance.setStatus("IN_PROGRESS");
        workflowInstance.setStartedAt(LocalDateTime.now());
        workflowInstance.setUpdatedBy("system"); //todo: get from security context
        // TODO: Publish workflow started event
        // workflowEventPublisher.publishWorkflowStarted(workflowInstance);
        
        // TODO: Create initial tasks based on workflow definition
        // List<Task> initialTasks = taskService.createInitialTasks(workflowInstance);
        
        // TODO: Assign tasks to users/roles based on workflow configuration
        // taskAssignmentService.assignTasks(initialTasks);
        
        // TODO: Send notifications to assigned users
        // notificationService.notifyAssignedUsers(initialTasks);
        
        // TODO: Update workflow state in cache
        // workflowStateCache.updateState(workflowInstance.getId(), "IN_PROGRESS");
        
        // TODO: Create audit log entry
        // auditService.logWorkflowStarted(workflowInstance);
        
        // TODO: Check for any automated tasks that can be executed
        // automatedTaskProcessor.processAutomatedTasks(workflowInstance);
        
        // TODO: Initialize workflow metrics
        // metricsService.initializeWorkflowMetrics(workflowInstance);
        WorkflowInstance savedInstance = workflowInstanceRepository.save(workflowInstance);
        
        logger.info("Started workflow instance: {}", id);
        return convertToDto(savedInstance);
    }
    
    /**
     * Cancel workflow instance
     */
    public WorkflowInstanceDto cancelWorkflowInstance(Long id) {
        logger.info("Cancelling workflow instance: {}", id);
        
        WorkflowInstance workflowInstance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow instance not found with id: " + id));
        
        if ("COMPLETED".equals(workflowInstance.getStatus()) || "REJECTED".equals(workflowInstance.getStatus())) {
            throw new IllegalStateException("Cannot cancel workflow instance with status: " + workflowInstance.getStatus());
        }
        
        workflowInstance.setStatus("CANCELLED");
        workflowInstance.setCompletedAt(LocalDateTime.now());
        workflowInstance.setUpdatedBy("system");
        
        WorkflowInstance savedInstance = workflowInstanceRepository.save(workflowInstance);
        
        logger.info("Cancelled workflow instance: {}", id);
        return convertToDto(savedInstance);
    }
    
    /**
     * Convert WorkflowInstance entity to DTO
     */
    private WorkflowInstanceDto convertToDto(WorkflowInstance workflowInstance) {
        WorkflowInstanceDto dto = new WorkflowInstanceDto();
        dto.setId(workflowInstance.getId());
        dto.setEntityType(workflowInstance.getEntityType());
        dto.setEntityId(workflowInstance.getEntityId());
        dto.setStatus(workflowInstance.getStatus());
        dto.setCreatedBy(workflowInstance.getCreatedBy());
        dto.setUpdatedBy(workflowInstance.getUpdatedBy());
        
        if (workflowInstance.getWorkflow() != null) {
            dto.setWorkflowId(workflowInstance.getWorkflow().getId());
            dto.setWorkflowName(workflowInstance.getWorkflow().getName());
        }
        
        return dto;
    }
}
