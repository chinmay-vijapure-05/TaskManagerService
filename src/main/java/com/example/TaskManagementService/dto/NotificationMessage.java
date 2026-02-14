package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification message sent via WebSocket or system events")
public class NotificationMessage {

    @Schema(
            description = "Unique identifier of the notification",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String id;

    @Schema(
            description = "Short title of the notification",
            example = "Task Assigned"
    )
    private String title;

    @Schema(
            description = "Detailed notification message",
            example = "You have been assigned to the task 'Implement JWT Security'"
    )
    private String message;

    @Schema(
            description = "Notification type",
            example = "INFO",
            allowableValues = {"INFO", "SUCCESS", "WARNING", "ERROR"}
    )
    private String type;  // INFO, SUCCESS, WARNING, ERROR

    @Schema(
            description = "Identifier (email or ID) of the target user",
            example = "user@test.com"
    )
    private String userId;

    @Schema(
            description = "Timestamp when the notification was created",
            example = "2026-02-14T16:45:30"
    )
    private LocalDateTime timestamp;

    public NotificationMessage(String title, String message, String type, String userId) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }
}
