package com.example.TaskManagementService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;          // TASK_CREATED, TASK_UPDATED, TASK_DELETED, etc.
    private String action;        // CREATE, UPDATE, DELETE
    private Object payload;       // The actual data (TaskResponse, ProjectResponse, etc.)
    private String userId;        // Who triggered the action
    private Long projectId;       // Which project this relates to
    private LocalDateTime timestamp;

    public WebSocketMessage(String type, String action, Object payload, String userId, Long projectId) {
        this.type = type;
        this.action = action;
        this.payload = payload;
        this.userId = userId;
        this.projectId = projectId;
        this.timestamp = LocalDateTime.now();
    }
}