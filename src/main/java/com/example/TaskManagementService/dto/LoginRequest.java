package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        name = "LoginRequest",
        description = "Request payload for user authentication"
)
public class LoginRequest {

    @Schema(
            description = "Registered user email address",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Email
    private String email;

    @Schema(
            description = "User account password",
            example = "SecurePass123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String password;
}
