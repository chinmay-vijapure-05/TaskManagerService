package com.example.TaskManagementService.dto;

import com.example.TaskManagementService.entity.TaskPriority;
import com.example.TaskManagementService.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(
        name = "TaskRequest",
        description = "Request payload used to create or update a task"
)
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Schema(
            description = "Title of the task",
            example = "Implement JWT Authentication",
            minLength = 3,
            maxLength = 200,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Schema(
            description = "Detailed description of the task",
            example = "Implement JWT-based authentication using Spring Security and validate tokens via filter.",
            maxLength = 2000
    )
    private String description;

    @NotNull(message = "Project ID is required")
    @Schema(
            description = "ID of the project this task belongs to",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long projectId;

    @Schema(
            description = "User ID assigned to this task (optional)",
            example = "2"
    )
    private Long assigneeId;

    @Schema(
            description = "Current task status",
            example = "TODO",
            allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED"}
    )
    private TaskStatus status;

    @Schema(
            description = "Task priority level",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"}
    )
    private TaskPriority priority;

    @Schema(
            description = "Due date and time for task completion (ISO-8601 format)",
            example = "2026-02-20T18:30:00"
    )
    private LocalDateTime dueDate;
}
