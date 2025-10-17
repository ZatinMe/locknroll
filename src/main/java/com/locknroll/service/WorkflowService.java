package com.locknroll.service;

import com.locknroll.dto.WorkflowDto;
import com.locknroll.dto.WorkflowStepDto;
import com.locknroll.dto.WorkflowStepDependencyDto;
import com.locknroll.entity.Workflow;
import com.locknroll.entity.WorkflowStep;
import com.locknroll.entity.WorkflowStepDependency;
import com.locknroll.entity.Role;
import com.locknroll.repository.WorkflowRepository;
import com.locknroll.repository.WorkflowStepRepository;
import com.locknroll.repository.RoleRepository;
import com.locknroll.exception.ResourceNotFoundException;
import com.locknroll.exception.WorkflowAlreadyExistsException;
import com.locknroll.exception.InvalidWorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Workflow operations
 */
@Service
@Transactional
public class WorkflowService {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowService.class);
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private WorkflowStepRepository workflowStepRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    /**
     * Create a new workflow
     */
    public WorkflowDto createWorkflow(WorkflowDto workflowDto) {
        logger.info("Creating workflow: {}", workflowDto.getName());
        
        // Check if workflow already exists
        if (workflowRepository.existsByName(workflowDto.getName())) {
            throw new WorkflowAlreadyExistsException("Workflow with name '" + workflowDto.getName() + "' already exists");
        }
        
        // Create workflow entity
        Workflow workflow = new Workflow();
        workflow.setName(workflowDto.getName());
        workflow.setDescription(workflowDto.getDescription());
        workflow.setEntityType(workflowDto.getEntityType());
        workflow.setIsActive(workflowDto.getIsActive() != null ? workflowDto.getIsActive() : true);
        workflow.setCreatedBy("system"); // TODO: Get from security context
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        
        // Create workflow steps if provided
        if (workflowDto.getSteps() != null && !workflowDto.getSteps().isEmpty()) {
            createWorkflowSteps(savedWorkflow, workflowDto.getSteps());
        }
        
        logger.info("Created workflow: {}", savedWorkflow.getName());
        return convertToDto(savedWorkflow);
    }
    
    /**
     * Get workflow by ID
     */
    @Transactional(readOnly = true)
    public WorkflowDto getWorkflowById(Long id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));
        
        return convertToDto(workflow);
    }
    
    /**
     * Get workflow by name
     */
    @Transactional(readOnly = true)
    public WorkflowDto getWorkflowByName(String name) {
        Workflow workflow = workflowRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with name: " + name));
        
        return convertToDto(workflow);
    }
    
    /**
     * Get all workflows
     */
    @Transactional(readOnly = true)
    public List<WorkflowDto> getAllWorkflows() {
        List<Workflow> workflows = workflowRepository.findAll();
        return workflows.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active workflows
     */
    @Transactional(readOnly = true)
    public List<WorkflowDto> getActiveWorkflows() {
        List<Workflow> workflows = workflowRepository.findByIsActiveTrue();
        return workflows.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get workflows by entity type
     */
    @Transactional(readOnly = true)
    public List<WorkflowDto> getWorkflowsByEntityType(String entityType) {
        List<Workflow> workflows = workflowRepository.findByEntityTypeAndIsActiveTrue(entityType);
        return workflows.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Update workflow
     */
    public WorkflowDto updateWorkflow(Long id, WorkflowDto workflowDto) {
        logger.info("Updating workflow: {}", id);
        
        Workflow existingWorkflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));
        
        // Update basic fields
        existingWorkflow.setName(workflowDto.getName());
        existingWorkflow.setDescription(workflowDto.getDescription());
        existingWorkflow.setEntityType(workflowDto.getEntityType());
        existingWorkflow.setIsActive(workflowDto.getIsActive());
        existingWorkflow.setUpdatedBy("system"); // TODO: Get from security context
        
        Workflow savedWorkflow = workflowRepository.save(existingWorkflow);
        
        logger.info("Updated workflow: {}", savedWorkflow.getName());
        return convertToDto(savedWorkflow);
    }
    
    /**
     * Delete workflow
     */
    public void deleteWorkflow(Long id) {
        logger.info("Deleting workflow: {}", id);
        
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));
        
        workflowRepository.deleteById(id);
        logger.info("Deleted workflow: {}", workflow.getName());
    }
    
    /**
     * Activate workflow
     */
    public WorkflowDto activateWorkflow(Long id) {
        logger.info("Activating workflow: {}", id);
        
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));
        
        workflow.setIsActive(true);
        workflow.setUpdatedBy("system"); // TODO: Get from security context
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        logger.info("Activated workflow: {}", savedWorkflow.getName());
        
        return convertToDto(savedWorkflow);
    }
    
    /**
     * Deactivate workflow
     */
    public WorkflowDto deactivateWorkflow(Long id) {
        logger.info("Deactivating workflow: {}", id);
        
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));
        
        workflow.setIsActive(false);
        workflow.setUpdatedBy("system"); // TODO: Get from security context
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        logger.info("Deactivated workflow: {}", savedWorkflow.getName());
        
        return convertToDto(savedWorkflow);
    }
    
    /**
     * Validate workflow configuration
     */
    @Transactional(readOnly = true)
    public void validateWorkflow(Long workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));
        
        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrder(workflowId);
        
        if (steps.isEmpty()) {
            throw new InvalidWorkflowException("Workflow must have at least one step");
        }
        
        // Validate step order
        for (int i = 0; i < steps.size(); i++) {
            WorkflowStep step = steps.get(i);
            if (step.getStepOrder() != i + 1) {
                throw new InvalidWorkflowException("Step order must be sequential starting from 1");
            }
        }
        
        // Validate role assignments
        for (WorkflowStep step : steps) {
            if (step.getAssignedRole() == null) {
                throw new InvalidWorkflowException("All steps must have an assigned role");
            }
        }
        
        logger.info("Workflow validation passed: {}", workflow.getName());
    }
    
    /**
     * Create workflow steps
     */
    private void createWorkflowSteps(Workflow workflow, List<WorkflowStepDto> stepDtos) {
        List<WorkflowStep> stepsToSave = new ArrayList<>();
        
        for (WorkflowStepDto stepDto : stepDtos) {
            WorkflowStep step = new WorkflowStep();
            step.setWorkflow(workflow);
            step.setStepOrder(stepDto.getStepOrder());
            step.setName(stepDto.getStepName());
            step.setDescription(stepDto.getDescription());
            step.setStepType(stepDto.getStepType());
            step.setIsRequired(stepDto.getIsRequired() != null ? stepDto.getIsRequired() : true);
            step.setIsActive(stepDto.getIsActive() != null ? stepDto.getIsActive() : true);
            step.setCreatedBy("system"); // TODO: Get from security context
            
            // Set assigned role
            if (stepDto.getAssignedRoleId() != null) {
                Role role = roleRepository.findById(stepDto.getAssignedRoleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + stepDto.getAssignedRoleId()));
                step.setAssignedRole(role);
            }
            
            stepsToSave.add(step);
        }
        
        workflowStepRepository.saveAll(stepsToSave);
    }
    
    /**
     * Convert Workflow entity to DTO
     */
    private WorkflowDto convertToDto(Workflow workflow) {
        WorkflowDto dto = new WorkflowDto();
        dto.setId(workflow.getId());
        dto.setName(workflow.getName());
        dto.setDescription(workflow.getDescription());
        dto.setEntityType(workflow.getEntityType());
        dto.setIsActive(workflow.getIsActive());
        dto.setCreatedBy(workflow.getCreatedBy());
        dto.setUpdatedBy(workflow.getUpdatedBy());
        
        // Convert steps
        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByStepOrder(workflow.getId());
        List<WorkflowStepDto> stepDtos = steps.stream()
                .map(this::convertStepToDto)
                .collect(Collectors.toList());
        dto.setSteps(stepDtos);
        
        return dto;
    }
    
    /**
     * Convert WorkflowStep entity to DTO
     */
    private WorkflowStepDto convertStepToDto(WorkflowStep step) {
        WorkflowStepDto dto = new WorkflowStepDto();
        dto.setId(step.getId());
        dto.setStepOrder(step.getStepOrder());
        dto.setStepName(step.getName());
        dto.setDescription(step.getDescription());
        dto.setStepType(step.getStepType());
        dto.setIsRequired(step.getIsRequired());
        dto.setIsActive(step.getIsActive());
        dto.setCreatedBy(step.getCreatedBy());
        dto.setUpdatedBy(step.getUpdatedBy());
        
        if (step.getAssignedRole() != null) {
            dto.setAssignedRoleId(step.getAssignedRole().getId());
            dto.setAssignedRoleName(step.getAssignedRole().getName());
        }
        
        return dto;
    }
}
