package com.example.TaskManagementService.dto;

import com.example.TaskManagementService.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ProjectResponse",
        description = "Response payload representing project details"
)
public class ProjectResponse {

    @Schema(description = "Unique identifier of the project", example = "1")
    private Long id;

    @Schema(description = "Project name", example = "AI Development Platform")
    private String name;

    @Schema(description = "Project description",
            example = "A collaborative backend platform for managing AI-driven services.")
    private String description;

    @Schema(description = "Email of the project owner", example = "owner@test.com")
    private String ownerEmail;

    @Schema(description = "Full name of the project owner", example = "Chinmay Vijapure")
    private String ownerName;

    @ArraySchema(
            schema = @Schema(implementation = MemberDto.class),
            arraySchema = @Schema(description = "List of project members")
    )
    private List<MemberDto> members;

    @Schema(
            description = "Current project status",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "COMPLETED", "ARCHIVED"}
    )
    private ProjectStatus status;

    @Schema(description = "Total number of tasks in this project", example = "12")
    private Integer taskCount;

    @Schema(
            description = "Project creation timestamp",
            example = "2026-02-14T10:15:30"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Last update timestamp",
            example = "2026-02-15T08:22:10"
    )
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @Schema(description = "Represents a project member")
    public static class MemberDto {

        @Schema(description = "Member user ID", example = "2")
        private Long id;

        @Schema(description = "Member email address", example = "member@test.com")
        private String email;

        @Schema(description = "Member full name", example = "John Doe")
        private String fullName;
    }
}
