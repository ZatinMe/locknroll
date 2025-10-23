package com.locknroll.config;

import com.locknroll.entity.Workflow;
import com.locknroll.entity.WorkflowStep;
import com.locknroll.entity.Role;
import com.locknroll.repository.WorkflowRepository;
import com.locknroll.repository.WorkflowStepRepository;
import com.locknroll.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Initialize default workflows and workflow steps
 */
@Component
@Order(3) // Run after UserDataInitializer
public class WorkflowDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowDataInitializer.class);
    
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private WorkflowStepRepository workflowStepRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing default workflows...");
        
        // Create default workflows
        createFruitApprovalWorkflow();
        createSellerOnboardingWorkflow();
        createSimpleApprovalWorkflow();
        
        logger.info("Default workflows initialized successfully");
    }
    
    /**
     * Create the standard fruit approval workflow: Finance -> Quality -> Manager
     */
    private void createFruitApprovalWorkflow() {
        String workflowName = "Fruit Approval Workflow";
        
        if (workflowRepository.existsByName(workflowName)) {
            logger.info("Workflow '{}' already exists, skipping creation", workflowName);
            return;
        }
        
        // Create workflow
        Workflow workflow = new Workflow();
        workflow.setName(workflowName);
        workflow.setDescription("Standard approval workflow for new fruits: Finance -> Quality -> Manager");
        workflow.setEntityType("FRUIT");
        workflow.setIsActive(true);
        workflow.setCreatedBy("system");
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        logger.info("Created workflow: {}", savedWorkflow.getName());
        
        // Get roles
        Role financeRole = roleRepository.findByName("FINANCE_ROLE")
                .orElseThrow(() -> new RuntimeException("FINANCE_ROLE not found"));
        Role qualityRole = roleRepository.findByName("QUALITY_ROLE")
                .orElseThrow(() -> new RuntimeException("QUALITY_ROLE not found"));
        Role managerRole = roleRepository.findByName("MANAGER_ROLE")
                .orElseThrow(() -> new RuntimeException("MANAGER_ROLE not found"));
        
        // Create workflow steps
        createWorkflowStep(savedWorkflow, 1, "Finance Review", "Finance team reviews pricing and cost", 
                          "APPROVAL", financeRole, true);
        createWorkflowStep(savedWorkflow, 2, "Quality Check", "Quality team checks fruit standards", 
                          "APPROVAL", qualityRole, true);
        createWorkflowStep(savedWorkflow, 3, "Manager Approval", "Manager gives final approval", 
                          "APPROVAL", managerRole, true);
    }
    
    /**
     * Create seller onboarding workflow
     */
    private void createSellerOnboardingWorkflow() {
        String workflowName = "Seller Onboarding Workflow";
        
        if (workflowRepository.existsByName(workflowName)) {
            logger.info("Workflow '{}' already exists, skipping creation", workflowName);
            return;
        }
        
        // Create workflow
        Workflow workflow = new Workflow();
        workflow.setName(workflowName);
        workflow.setDescription("Workflow for onboarding new sellers");
        workflow.setEntityType("SELLER");
        workflow.setIsActive(true);
        workflow.setCreatedBy("system");
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        logger.info("Created workflow: {}", savedWorkflow.getName());
        
        // Get roles
        Role backofficeRole = roleRepository.findByName("BACKOFFICE_ROLE")
                .orElseThrow(() -> new RuntimeException("BACKOFFICE_ROLE not found"));
        Role managerRole = roleRepository.findByName("MANAGER_ROLE")
                .orElseThrow(() -> new RuntimeException("MANAGER_ROLE not found"));
        
        // Create workflow steps
        createWorkflowStep(savedWorkflow, 1, "Document Review", "Back office reviews seller documents", 
                          "APPROVAL", backofficeRole, true);
        createWorkflowStep(savedWorkflow, 2, "Manager Approval", "Manager approves seller onboarding", 
                          "APPROVAL", managerRole, true);
    }
    
    /**
     * Create a simple approval workflow for testing
     */
    private void createSimpleApprovalWorkflow() {
        String workflowName = "Simple Approval Workflow";
        
        if (workflowRepository.existsByName(workflowName)) {
            logger.info("Workflow '{}' already exists, skipping creation", workflowName);
            return;
        }
        
        // Create workflow
        Workflow workflow = new Workflow();
        workflow.setName(workflowName);
        workflow.setDescription("Simple single-step approval workflow for testing");
        workflow.setEntityType("GENERIC");
        workflow.setIsActive(true);
        workflow.setCreatedBy("system");
        
        Workflow savedWorkflow = workflowRepository.save(workflow);
        logger.info("Created workflow: {}", savedWorkflow.getName());
        
        // Get role
        Role managerRole = roleRepository.findByName("MANAGER_ROLE")
                .orElseThrow(() -> new RuntimeException("MANAGER_ROLE not found"));
        
        // Create single workflow step
        createWorkflowStep(savedWorkflow, 1, "Manager Approval", "Manager approval for generic items", 
                          "APPROVAL", managerRole, true);
    }
    
    /**
     * Create a workflow step
     */
    private void createWorkflowStep(Workflow workflow, Integer stepOrder, String stepName, 
                                   String description, String stepType, Role assignedRole, Boolean isRequired) {
        WorkflowStep step = new WorkflowStep();
        step.setWorkflow(workflow);
        step.setStepOrder(stepOrder);
        step.setName(stepName);
        step.setDescription(description);
        step.setStepType(stepType);
        step.setAssignedRole(assignedRole);
        step.setAssignedRoleName(assignedRole.getName()); // Set the role name string for easier querying
        step.setIsRequired(isRequired);
        step.setIsActive(true);
        step.setCreatedBy("system");
        
        WorkflowStep savedStep = workflowStepRepository.save(step);
        logger.info("Created workflow step: {} - {} (Role: {})", stepOrder, stepName, assignedRole.getName());
    }
}
