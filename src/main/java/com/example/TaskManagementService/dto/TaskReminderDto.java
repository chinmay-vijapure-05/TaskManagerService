package com.example.TaskManagementService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskReminderDto {
    private Long taskId;
    private String taskTitle;
    private String assigneeEmail;
    private String assigneeName;
    private LocalDateTime dueDate;
    private String priority;
    private Long hoursUntilDue;
}