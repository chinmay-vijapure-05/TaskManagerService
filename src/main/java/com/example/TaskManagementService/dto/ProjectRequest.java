package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "ProjectRequest",
        description = "Request payload used to create or update a project"
)
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    @Schema(
            description = "Name of the project",
            example = "AI Development Platform",
            minLength = 3,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(
            description = "Detailed description of the project",
            example = "A collaborative backend platform for managing AI-driven microservices.",
            maxLength = 1000
    )
    private String description;

    @ArraySchema(
            schema = @Schema(
                    description = "User IDs of members to be added to the project",
                    example = "2"
            ),
            arraySchema = @Schema(
                    description = "Optional list of user IDs who will be added as project members"
            )
    )
    private List<Long> memberIds;
}
