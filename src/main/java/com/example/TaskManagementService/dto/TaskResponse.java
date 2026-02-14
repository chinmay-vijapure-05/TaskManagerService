package com.example.TaskManagementService.dto;

import com.example.TaskManagementService.entity.TaskPriority;
import com.example.TaskManagementService.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "TaskResponse",
        description = "Response object returned for task-related operations"
)
public class TaskResponse {

    @Schema(description = "Unique identifier of the task", example = "101")
    private Long id;

    @Schema(description = "Title of the task", example = "Implement JWT Authentication")
    private String title;

    @Schema(description = "Detailed description of the task",
            example = "Implement authentication using Spring Security and JWT.")
    private String description;

    @Schema(description = "ID of the associated project", example = "1")
    private Long projectId;

    @Schema(description = "Name of the associated project", example = "Task Management Platform")
    private String projectName;

    @Schema(description = "User assigned to the task")
    private AssigneeDto assignee;

    @Schema(description = "User who created the task")
    private AssigneeDto createdBy;

    @Schema(
            description = "Current task status",
            example = "IN_PROGRESS",
            allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED"}
    )
    private TaskStatus status;

    @Schema(
            description = "Priority level of the task",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"}
    )
    private TaskPriority priority;

    @Schema(
            description = "Due date and time of the task (ISO-8601 format)",
            example = "2026-02-20T18:30:00"
    )
    private LocalDateTime dueDate;

    @Schema(
            description = "Timestamp when the task was created",
            example = "2026-02-14T10:15:30"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Timestamp when the task was last updated",
            example = "2026-02-14T12:00:00"
    )
    private LocalDateTime updatedAt;


    @Data
    @AllArgsConstructor
    @Schema(
            name = "TaskAssignee",
            description = "Represents a user assigned to or associated with a task"
    )
    public static class AssigneeDto {

        @Schema(description = "User ID", example = "5")
        private Long id;

        @Schema(description = "User email address", example = "developer@test.com")
        private String email;

        @Schema(description = "User full name", example = "John Doe")
        private String fullName;
    }
}
