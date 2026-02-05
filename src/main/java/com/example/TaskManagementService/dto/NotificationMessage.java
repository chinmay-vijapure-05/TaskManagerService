package com.example.TaskManagementService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String id;
    private String title;
    private String message;
    private String type;  // INFO, SUCCESS, WARNING, ERROR
    private String userId;
    private LocalDateTime timestamp;

    public NotificationMessage(String title, String message, String type, String userId) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }
}