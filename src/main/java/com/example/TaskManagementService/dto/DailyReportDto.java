package com.example.TaskManagementService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportDto {
    private LocalDate reportDate;
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int overdueTasks;
    private Map<String, Integer> tasksByStatus;
    private Map<String, Integer> tasksByPriority;
}