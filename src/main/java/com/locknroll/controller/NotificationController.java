package com.locknroll.controller;

import com.locknroll.dto.NotificationDto;
import com.locknroll.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for notification management
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * Send notification to a specific user
     */
    @PostMapping("/send/user")
    public ResponseEntity<?> sendNotificationToUser(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String type = (String) request.get("type");
            String priority = (String) request.get("priority");

            if (username == null || title == null || message == null) {
                return ResponseEntity.badRequest().body("Missing required fields: username, title, message");
            }

            NotificationDto notification = new NotificationDto();
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type != null ? type : "SYSTEM");
            notification.setPriority(priority != null ? priority : "MEDIUM");

            notificationService.sendNotificationToUser(username, notification);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            logger.error("Error sending notification to user: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error sending notification: " + e.getMessage());
        }
    }

    /**
     * Send notification to a specific role
     */
    @PostMapping("/send/role")
    public ResponseEntity<?> sendNotificationToRole(@RequestBody Map<String, Object> request) {
        try {
            String role = (String) request.get("role");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String type = (String) request.get("type");
            String priority = (String) request.get("priority");

            if (role == null || title == null || message == null) {
                return ResponseEntity.badRequest().body("Missing required fields: role, title, message");
            }

            NotificationDto notification = new NotificationDto();
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type != null ? type : "SYSTEM");
            notification.setPriority(priority != null ? priority : "MEDIUM");

            notificationService.sendNotificationToRole(role, notification);
            return ResponseEntity.ok("Notification sent to role successfully");
        } catch (Exception e) {
            logger.error("Error sending notification to role: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error sending notification: " + e.getMessage());
        }
    }

    /**
     * Send broadcast notification
     */
    @PostMapping("/send/broadcast")
    public ResponseEntity<?> sendBroadcastNotification(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String type = (String) request.get("type");
            String priority = (String) request.get("priority");

            if (title == null || message == null) {
                return ResponseEntity.badRequest().body("Missing required fields: title, message");
            }

            NotificationDto notification = new NotificationDto();
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type != null ? type : "SYSTEM");
            notification.setPriority(priority != null ? priority : "MEDIUM");

            notificationService.sendBroadcastNotification(notification);
            return ResponseEntity.ok("Broadcast notification sent successfully");
        } catch (Exception e) {
            logger.error("Error sending broadcast notification: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error sending notification: " + e.getMessage());
        }
    }

    /**
     * Get active user sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> getActiveSessions() {
        try {
            Map<String, String> sessions = notificationService.getActiveSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            logger.error("Error getting active sessions: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error getting sessions: " + e.getMessage());
        }
    }

    /**
     * Test notification endpoint
     */
    @PostMapping("/test")
    public ResponseEntity<?> testNotification() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "system";

            NotificationDto testNotification = new NotificationDto();
            testNotification.setTitle("Test Notification");
            testNotification.setMessage("This is a test notification from LockNRoll");
            testNotification.setType("SYSTEM");
            testNotification.setPriority("LOW");

            notificationService.sendNotificationToUser(username, testNotification);
            return ResponseEntity.ok("Test notification sent to " + username);
        } catch (Exception e) {
            logger.error("Error sending test notification: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error sending test notification: " + e.getMessage());
        }
    }
}
