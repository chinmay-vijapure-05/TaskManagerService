package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(
        name = "RegisterRequest",
        description = "Request payload for user registration"
)
public class RegisterRequest {

    @Schema(
            description = "User email address (must be unique)",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Email
    private String email;

    @Schema(
            description = "User password (minimum 6 characters)",
            example = "SecurePass123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 6)
    private String password;

    @Schema(
            description = "Full name of the user",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String fullName;
}
