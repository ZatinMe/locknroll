package com.locknroll.service;

import com.locknroll.dto.DashboardDto;
import com.locknroll.dto.TaskDto;
import com.locknroll.entity.Fruit;
import com.locknroll.entity.Task;
import com.locknroll.entity.WorkflowInstance;
import com.locknroll.repository.FruitRepository;
import com.locknroll.repository.TaskRepository;
import com.locknroll.repository.WorkflowInstanceRepository;
import com.locknroll.security.CustomUserDetailsService.CustomUserPrincipal;
import com.locknroll.security.RoleBasedAccessControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard operations
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private RoleBasedAccessControl rbac;

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private TaskService taskService;

    /**
     * Get dashboard data for current user
     */
    public DashboardDto getDashboard() {
        CustomUserPrincipal user = rbac.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User not authenticated");
        }

        DashboardDto dashboard = new DashboardDto();
        dashboard.setUsername(user.getUsername());
        dashboard.setFullName(user.getFullName());
        dashboard.setRoles(rbac.getUserRoles());
        dashboard.setLastUpdated(LocalDateTime.now());

        // Determine user type and get appropriate dashboard data
        if (rbac.isAdmin() || rbac.isBackOffice()) {
            dashboard.setUserType("ADMIN");
            populateAdminDashboard(dashboard);
        } else if (rbac.isSeller()) {
            dashboard.setUserType("SELLER");
            populateSellerDashboard(dashboard, user.getUsername());
        } else if (rbac.isApprover()) {
            dashboard.setUserType("APPROVER");
            populateApproverDashboard(dashboard, user.getUserId());
        } else {
            dashboard.setUserType("USER");
            populateUserDashboard(dashboard);
        }

        return dashboard;
    }

    /**
     * Populate admin/back office dashboard
     */
    private void populateAdminDashboard(DashboardDto dashboard) {
        Map<String, Object> stats = new HashMap<>();
        
        // System statistics
        long totalFruits = fruitRepository.count();
        long draftFruits = fruitRepository.findByStatus("DRAFT").size();
        long pendingFruits = fruitRepository.findByStatus("PENDING_APPROVAL").size();
        long approvedFruits = fruitRepository.findByStatus("APPROVED").size();
        long rejectedFruits = fruitRepository.findByStatus("REJECTED").size();
        
        long totalWorkflows = workflowInstanceRepository.count();
        long activeWorkflows = workflowInstanceRepository.countActiveWorkflowInstances();
        long completedWorkflows = workflowInstanceRepository.findCompletedWorkflowInstances().size();
        
        long totalTasks = taskRepository.count();
        long pendingTasks = taskRepository.findByStatus("PENDING").size();
        long completedTasks = taskRepository.findByStatus("COMPLETED").size();

        stats.put("totalFruits", totalFruits);
        stats.put("draftFruits", draftFruits);
        stats.put("pendingFruits", pendingFruits);
        stats.put("approvedFruits", approvedFruits);
        stats.put("rejectedFruits", rejectedFruits);
        stats.put("totalWorkflows", totalWorkflows);
        stats.put("activeWorkflows", activeWorkflows);
        stats.put("completedWorkflows", completedWorkflows);
        stats.put("totalTasks", totalTasks);
        stats.put("pendingTasks", pendingTasks);
        stats.put("completedTasks", completedTasks);

        dashboard.setStatistics(stats);

        // Recent activities (simplified)
        List<Object> activities = new ArrayList<>();
        activities.add("System overview loaded");
        dashboard.setRecentActivities(activities);

        // All pending tasks
        List<Task> allPendingTasks = taskRepository.findByStatus("PENDING");
        List<TaskDto> pendingTaskDtos = allPendingTasks.stream()
                .map(taskService::convertToDto)
                .collect(Collectors.toList());
        dashboard.setPendingTasks(new ArrayList<>(pendingTaskDtos));

        // Notifications
        List<Object> notifications = new ArrayList<>();
        if (pendingFruits > 0) {
            notifications.add(Map.of("type", "warning", "message", pendingFruits + " fruits pending approval"));
        }
        if (activeWorkflows > 0) {
            notifications.add(Map.of("type", "info", "message", activeWorkflows + " active workflows"));
        }
        dashboard.setNotifications(notifications);
    }

    /**
     * Populate seller dashboard
     */
    private void populateSellerDashboard(DashboardDto dashboard, String username) {
        Map<String, Object> stats = new HashMap<>();
        
        // Seller-specific statistics
        List<Fruit> userFruits = fruitRepository.findByCreatedBy(username);
        long totalFruits = userFruits.size();
        long draftFruits = userFruits.stream().filter(f -> "DRAFT".equals(f.getStatus())).count();
        long pendingFruits = userFruits.stream().filter(f -> "PENDING_APPROVAL".equals(f.getStatus())).count();
        long approvedFruits = userFruits.stream().filter(f -> "APPROVED".equals(f.getStatus())).count();
        long rejectedFruits = userFruits.stream().filter(f -> "REJECTED".equals(f.getStatus())).count();

        stats.put("totalFruits", totalFruits);
        stats.put("draftFruits", draftFruits);
        stats.put("pendingFruits", pendingFruits);
        stats.put("approvedFruits", approvedFruits);
        stats.put("rejectedFruits", rejectedFruits);

        dashboard.setStatistics(stats);

        // Recent activities
        List<Object> activities = new ArrayList<>();
        activities.add("Dashboard loaded for seller: " + username);
        dashboard.setRecentActivities(activities);

        // Seller's pending tasks (if any)
        List<Task> userTasks = taskRepository.findByAssignedToId(rbac.getCurrentUserId());
        List<TaskDto> pendingTaskDtos = userTasks.stream()
                .filter(task -> "PENDING".equals(task.getStatus()))
                .map(taskService::convertToDto)
                .collect(Collectors.toList());
        dashboard.setPendingTasks(new ArrayList<>(pendingTaskDtos));

        // Notifications
        List<Object> notifications = new ArrayList<>();
        if (pendingFruits > 0) {
            notifications.add(Map.of("type", "info", "message", pendingFruits + " fruits pending approval"));
        }
        if (rejectedFruits > 0) {
            notifications.add(Map.of("type", "warning", "message", rejectedFruits + " fruits rejected"));
        }
        dashboard.setNotifications(notifications);
    }

    /**
     * Populate approver dashboard
     */
    private void populateApproverDashboard(DashboardDto dashboard, Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Approver-specific statistics
        List<Task> userTasks = taskRepository.findByAssignedToId(userId);
        long totalTasks = userTasks.size();
        long pendingTasks = userTasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
        long completedTasks = userTasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long rejectedTasks = userTasks.stream().filter(t -> "REJECTED".equals(t.getStatus())).count();

        stats.put("totalTasks", totalTasks);
        stats.put("pendingTasks", pendingTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("rejectedTasks", rejectedTasks);

        dashboard.setStatistics(stats);

        // Recent activities
        List<Object> activities = new ArrayList<>();
        activities.add("Dashboard loaded for approver");
        dashboard.setRecentActivities(activities);

        // Approver's pending tasks
        List<TaskDto> pendingTaskDtos = userTasks.stream()
                .filter(task -> "PENDING".equals(task.getStatus()))
                .map(taskService::convertToDto)
                .collect(Collectors.toList());
        dashboard.setPendingTasks(new ArrayList<>(pendingTaskDtos));

        // Notifications
        List<Object> notifications = new ArrayList<>();
        if (pendingTasks > 0) {
            notifications.add(Map.of("type", "warning", "message", pendingTasks + " tasks pending approval"));
        }
        dashboard.setNotifications(notifications);
    }

    /**
     * Populate general user dashboard
     */
    private void populateUserDashboard(DashboardDto dashboard) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "Welcome to the system");
        dashboard.setStatistics(stats);

        List<Object> activities = new ArrayList<>();
        activities.add("Dashboard loaded");
        dashboard.setRecentActivities(activities);

        dashboard.setPendingTasks(new ArrayList<>());
        dashboard.setNotifications(new ArrayList<>());
    }

    /**
     * Get seller-specific dashboard data
     */
    public DashboardDto getSellerDashboard(String username) {
        if (!rbac.isSeller() && !rbac.isAdmin() && !rbac.isBackOffice()) {
            throw new RuntimeException("Access denied");
        }

        DashboardDto dashboard = new DashboardDto();
        dashboard.setUsername(username);
        dashboard.setUserType("SELLER");
        dashboard.setLastUpdated(LocalDateTime.now());

        populateSellerDashboard(dashboard, username);
        return dashboard;
    }

    /**
     * Get approver-specific dashboard data
     */
    public DashboardDto getApproverDashboard(Long userId) {
        if (!rbac.isApprover() && !rbac.isAdmin() && !rbac.isBackOffice()) {
            throw new RuntimeException("Access denied");
        }

        DashboardDto dashboard = new DashboardDto();
        dashboard.setUserType("APPROVER");
        dashboard.setLastUpdated(LocalDateTime.now());

        populateApproverDashboard(dashboard, userId);
        return dashboard;
    }
}
