package com.example.TaskManagementService.dto;

import com.example.TaskManagementService.entity.TaskPriority;
import com.example.TaskManagementService.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private String projectName;
    private AssigneeDto assignee;
    private AssigneeDto createdBy;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    public static class AssigneeDto {
        private Long id;
        private String email;
        private String fullName;
    }
}