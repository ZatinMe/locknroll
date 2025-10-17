package com.locknroll.controller;

import com.locknroll.dto.DashboardDto;
import com.locknroll.security.SecurityAnnotations;
import com.locknroll.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for dashboard operations
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get current user's dashboard
     */
    @GetMapping
    @SecurityAnnotations.Authenticated
    public ResponseEntity<DashboardDto> getDashboard() {
        logger.info("Fetching dashboard for current user");
        try {
            DashboardDto dashboard = dashboardService.getDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Error fetching dashboard", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get seller dashboard (admin/backoffice only)
     */
    @GetMapping("/seller/{username}")
    @SecurityAnnotations.AdminOrBackOffice
    public ResponseEntity<DashboardDto> getSellerDashboard(@PathVariable String username) {
        logger.info("Fetching seller dashboard for user: {}", username);
        try {
            DashboardDto dashboard = dashboardService.getSellerDashboard(username);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Error fetching seller dashboard for user: {}", username, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get approver dashboard (admin/backoffice only)
     */
    @GetMapping("/approver/{userId}")
    @SecurityAnnotations.AdminOrBackOffice
    public ResponseEntity<DashboardDto> getApproverDashboard(@PathVariable Long userId) {
        logger.info("Fetching approver dashboard for user ID: {}", userId);
        try {
            DashboardDto dashboard = dashboardService.getApproverDashboard(userId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Error fetching approver dashboard for user ID: {}", userId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get admin dashboard statistics
     */
    @GetMapping("/admin/stats")
    @SecurityAnnotations.AdminOrBackOffice
    public ResponseEntity<DashboardDto> getAdminStats() {
        logger.info("Fetching admin dashboard statistics");
        try {
            DashboardDto dashboard = dashboardService.getDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Error fetching admin dashboard statistics", e);
            return ResponseEntity.status(500).build();
        }
    }
}
