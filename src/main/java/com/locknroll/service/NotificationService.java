package com.locknroll.service;

import com.locknroll.dto.NotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for sending real-time notifications via WebSocket
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Store user sessions for targeted notifications
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    /**
     * Send notification to a specific user
     */
    public void sendNotificationToUser(String username, NotificationDto notification) {
        try {
            String destination = "/queue/notifications/" + username;
            messagingTemplate.convertAndSend(destination, notification);
            logger.info("Notification sent to user {}: {}", username, notification.getTitle());
        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Send notification to all users with a specific role
     */
    public void sendNotificationToRole(String role, NotificationDto notification) {
        try {
            String destination = "/topic/notifications/role/" + role;
            messagingTemplate.convertAndSend(destination, notification);
            logger.info("Notification sent to role {}: {}", role, notification.getTitle());
        } catch (Exception e) {
            logger.error("Failed to send notification to role {}: {}", role, e.getMessage());
        }
    }

    /**
     * Send notification to all users
     */
    public void sendBroadcastNotification(NotificationDto notification) {
        try {
            String destination = "/topic/notifications/broadcast";
            messagingTemplate.convertAndSend(destination, notification);
            logger.info("Broadcast notification sent: {}", notification.getTitle());
        } catch (Exception e) {
            logger.error("Failed to send broadcast notification: {}", e.getMessage());
        }
    }

    /**
     * Send task update notification
     */
    public void sendTaskUpdateNotification(String username, String taskTitle, String status, String message) {
        NotificationDto notification = new NotificationDto();
        notification.setTitle("Task Update: " + taskTitle);
        notification.setMessage(message);
        notification.setType("TASK_UPDATE");
        notification.setPriority("MEDIUM");
        notification.setTimestamp(LocalDateTime.now());
        notification.setData(Map.of(
            "taskTitle", taskTitle,
            "status", status,
            "username", username
        ));

        sendNotificationToUser(username, notification);
    }

    /**
     * Send workflow status notification
     */
    public void sendWorkflowStatusNotification(String username, String workflowName, String status, String message) {
        NotificationDto notification = new NotificationDto();
        notification.setTitle("Workflow Update: " + workflowName);
        notification.setMessage(message);
        notification.setType("WORKFLOW_UPDATE");
        notification.setPriority("HIGH");
        notification.setTimestamp(LocalDateTime.now());
        notification.setData(Map.of(
            "workflowName", workflowName,
            "status", status,
            "username", username
        ));

        sendNotificationToUser(username, notification);
    }

    /**
     * Send fruit approval notification
     */
    public void sendFruitApprovalNotification(String username, String fruitName, String status, String message) {
        NotificationDto notification = new NotificationDto();
        notification.setTitle("Fruit Approval: " + fruitName);
        notification.setMessage(message);
        notification.setType("FRUIT_APPROVAL");
        notification.setPriority("HIGH");
        notification.setTimestamp(LocalDateTime.now());
        notification.setData(Map.of(
            "fruitName", fruitName,
            "status", status,
            "username", username
        ));

        sendNotificationToUser(username, notification);
    }

    /**
     * Send system notification
     */
    public void sendSystemNotification(String title, String message, String priority) {
        NotificationDto notification = new NotificationDto();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("SYSTEM");
        notification.setPriority(priority);
        notification.setTimestamp(LocalDateTime.now());

        sendBroadcastNotification(notification);
    }

    /**
     * Register user session
     */
    public void registerUserSession(String username, String sessionId) {
        userSessions.put(username, sessionId);
        logger.debug("User session registered: {} -> {}", username, sessionId);
    }

    /**
     * Unregister user session
     */
    public void unregisterUserSession(String username) {
        userSessions.remove(username);
        logger.debug("User session unregistered: {}", username);
    }

    /**
     * Get active user sessions
     */
    public Map<String, String> getActiveSessions() {
        return new ConcurrentHashMap<>(userSessions);
    }
}
