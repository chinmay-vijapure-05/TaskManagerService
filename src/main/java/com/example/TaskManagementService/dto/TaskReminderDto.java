package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reminder information for tasks approaching their due date")
public class TaskReminderDto {

    @Schema(
            description = "Unique ID of the task",
            example = "101"
    )
    private Long taskId;

    @Schema(
            description = "Title of the task",
            example = "Implement JWT authentication"
    )
    private String taskTitle;

    @Schema(
            description = "Email address of the assigned user",
            example = "developer@test.com"
    )
    private String assigneeEmail;

    @Schema(
            description = "Full name of the assigned user",
            example = "John Doe"
    )
    private String assigneeName;

    @Schema(
            description = "Due date and time of the task",
            example = "2026-02-15T18:00:00"
    )
    private LocalDateTime dueDate;

    @Schema(
            description = "Priority level of the task",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"}
    )
    private String priority;

    @Schema(
            description = "Number of hours remaining until the task is due",
            example = "5"
    )
    private Long hoursUntilDue;
}
