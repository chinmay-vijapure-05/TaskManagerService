package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic WebSocket event message used for real-time updates")
public class WebSocketMessage {

    @Schema(
            description = "Event type category",
            example = "TASK",
            allowableValues = {"TASK", "PROJECT", "SYSTEM"}
    )
    private String type;   // TASK, PROJECT, SYSTEM

    @Schema(
            description = "Action performed on the entity",
            example = "CREATE",
            allowableValues = {"CREATE", "UPDATE", "DELETE", "BROADCAST"}
    )
    private String action; // CREATE, UPDATE, DELETE, BROADCAST

    @Schema(
            description = "Actual payload data (TaskResponse, ProjectResponse, or other DTO)",
            example = "{ \"id\": 1, \"title\": \"New Task\" }"
    )
    private Object payload;

    @Schema(
            description = "Identifier (email or ID) of the user who triggered the event",
            example = "user@test.com"
    )
    private String userId;

    @Schema(
            description = "Associated project ID (if applicable)",
            example = "10"
    )
    private Long projectId;

    @Schema(
            description = "Timestamp when the event was generated",
            example = "2026-02-14T16:50:00"
    )
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