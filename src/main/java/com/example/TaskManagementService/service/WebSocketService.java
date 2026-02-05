package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.NotificationMessage;
import com.example.TaskManagementService.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send message to all users in a specific project
     */
    public void sendProjectUpdate(Long projectId, String type, String action, Object payload, String userId) {
        WebSocketMessage message = new WebSocketMessage(type, action, payload, userId, projectId);
        String destination = "/topic/project/" + projectId;

        log.info("Sending WebSocket message to {}: type={}, action={}", destination, type, action);
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Send notification to a specific user
     */
    public void sendUserNotification(String userEmail, NotificationMessage notification) {
        String destination = "/queue/notifications/" + userEmail;

        log.info("Sending notification to user {}: {}", userEmail, notification.getTitle());
        messagingTemplate.convertAndSend(destination, notification);
    }

    /**
     * Send task update to project subscribers
     */
    public void sendTaskUpdate(Long projectId, String action, Object taskResponse, String userId) {
        sendProjectUpdate(projectId, "TASK", action, taskResponse, userId);
    }

    /**
     * Send project update to project subscribers
     */
    public void sendProjectUpdateMessage(Long projectId, String action, Object projectResponse, String userId) {
        sendProjectUpdate(projectId, "PROJECT", action, projectResponse, userId);
    }

    /**
     * Broadcast message to all connected users
     */
    public void broadcastMessage(String type, Object payload) {
        WebSocketMessage message = new WebSocketMessage(type, "BROADCAST", payload, "SYSTEM", null);

        log.info("Broadcasting message: type={}", type);
        messagingTemplate.convertAndSend("/topic/broadcast", message);
    }
}