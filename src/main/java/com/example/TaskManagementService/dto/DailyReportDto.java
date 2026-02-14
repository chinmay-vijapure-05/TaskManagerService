package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Daily summary report containing task statistics")
public class DailyReportDto {

    @Schema(
            description = "Date for which the report is generated",
            example = "2026-02-14"
    )
    private LocalDate reportDate;

    @Schema(
            description = "Total number of tasks in the system",
            example = "42"
    )
    private int totalTasks;

    @Schema(
            description = "Number of completed tasks",
            example = "20"
    )
    private int completedTasks;

    @Schema(
            description = "Number of pending tasks (not completed)",
            example = "15"
    )
    private int pendingTasks;

    @Schema(
            description = "Number of overdue tasks",
            example = "7"
    )
    private int overdueTasks;

    @Schema(
            description = "Task distribution grouped by status (e.g., TODO, IN_PROGRESS, COMPLETED)",
            example = "{\"TODO\": 10, \"IN_PROGRESS\": 12, \"COMPLETED\": 20}"
    )
    private Map<String, Integer> tasksByStatus;

    @Schema(
            description = "Task distribution grouped by priority (e.g., LOW, MEDIUM, HIGH, URGENT)",
            example = "{\"LOW\": 5, \"MEDIUM\": 15, \"HIGH\": 12, \"URGENT\": 10}"
    )
    private Map<String, Integer> tasksByPriority;
}
