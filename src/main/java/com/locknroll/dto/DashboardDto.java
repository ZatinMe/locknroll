package com.locknroll.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for dashboard data
 */
public class DashboardDto {

    private String userType;
    private String username;
    private String fullName;
    private List<String> roles;
    private Map<String, Object> statistics;
    private List<Object> recentActivities;
    private List<Object> pendingTasks;
    private List<Object> notifications;
    private LocalDateTime lastUpdated;

    // Constructors
    public DashboardDto() {}

    // Getters and Setters
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Map<String, Object> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Object> statistics) {
        this.statistics = statistics;
    }

    public List<Object> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<Object> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public List<Object> getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(List<Object> pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public List<Object> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Object> notifications) {
        this.notifications = notifications;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
