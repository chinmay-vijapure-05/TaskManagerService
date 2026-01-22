package com.example.TaskManagementService.dto;

import com.example.TaskManagementService.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String ownerEmail;
    private String ownerName;
    private List<MemberDto> members;
    private ProjectStatus status;
    private Integer taskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    public static class MemberDto {
        private Long id;
        private String email;
        private String fullName;
    }
}