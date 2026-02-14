package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(
        name = "AuthResponse",
        description = "Response returned after successful authentication containing JWT token and user details"
)
public class AuthResponse {

    @Schema(
            description = "JWT access token. Must be included in Authorization header as 'Bearer <token>'",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    @Schema(
            description = "Authenticated user's email address",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "Authenticated user's full name",
            example = "John Doe"
    )
    private String fullName;
}
