package com.locknroll.controller;

import com.locknroll.dto.NotificationDto;
import com.locknroll.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket controller for real-time notifications
 */
@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * Handle user connection
     */
    @MessageMapping("/connect")
    @SendToUser("/queue/notifications")
    public NotificationDto handleUserConnection(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String username = principal.getName();
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("User connected: {} with session: {}", username, sessionId);
        
        // Register user session
        notificationService.registerUserSession(username, sessionId);
        
        // Send welcome notification
        NotificationDto welcomeNotification = new NotificationDto();
        welcomeNotification.setTitle("Welcome to LockNRoll");
        welcomeNotification.setMessage("You are now connected to real-time notifications");
        welcomeNotification.setType("SYSTEM");
        welcomeNotification.setPriority("LOW");
        welcomeNotification.setTimestamp(LocalDateTime.now());
        
        return welcomeNotification;
    }

    /**
     * Handle user disconnection
     */
    @MessageMapping("/disconnect")
    public void handleUserDisconnection(Principal principal) {
        String username = principal.getName();
        logger.info("User disconnected: {}", username);
        
        // Unregister user session
        notificationService.unregisterUserSession(username);
    }

    /**
     * Handle notification acknowledgment
     */
    @MessageMapping("/acknowledge")
    public void handleNotificationAcknowledgment(Principal principal, String notificationId) {
        String username = principal.getName();
        logger.debug("User {} acknowledged notification: {}", username, notificationId);
        
        // Here you could update the notification status in the database
        // For now, we'll just log it
    }

    /**
     * Handle subscription to role-based notifications
     */
    @MessageMapping("/subscribe/role")
    public void handleRoleSubscription(Principal principal, String role) {
        String username = principal.getName();
        logger.info("User {} subscribed to role notifications: {}", username, role);
        
        // Send confirmation notification
        NotificationDto confirmation = new NotificationDto();
        confirmation.setTitle("Subscription Confirmed");
        confirmation.setMessage("You are now subscribed to " + role + " notifications");
        confirmation.setType("SYSTEM");
        confirmation.setPriority("LOW");
        confirmation.setTimestamp(LocalDateTime.now());
        
        notificationService.sendNotificationToUser(username, confirmation);
    }

    /**
     * Handle subscription to workflow notifications
     */
    @MessageMapping("/subscribe/workflow")
    public void handleWorkflowSubscription(Principal principal, String workflowId) {
        String username = principal.getName();
        logger.info("User {} subscribed to workflow notifications: {}", username, workflowId);
        
        // Send confirmation notification
        NotificationDto confirmation = new NotificationDto();
        confirmation.setTitle("Workflow Subscription Confirmed");
        confirmation.setMessage("You are now subscribed to workflow " + workflowId + " notifications");
        confirmation.setType("SYSTEM");
        confirmation.setPriority("LOW");
        confirmation.setTimestamp(LocalDateTime.now());
        
        notificationService.sendNotificationToUser(username, confirmation);
    }
}
