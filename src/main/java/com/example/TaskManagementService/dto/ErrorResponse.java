package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response returned for failed API requests")
public class ErrorResponse {

    @Schema(
            description = "Timestamp when the error occurred",
            example = "2026-02-14T16:45:30"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "HTTP status code",
            example = "400"
    )
    private int status;

    @Schema(
            description = "HTTP error type",
            example = "Bad Request"
    )
    private String error;

    @Schema(
            description = "Detailed error message",
            example = "Invalid input data"
    )
    private String message;

    @Schema(
            description = "API endpoint path where the error occurred",
            example = "/api/projects"
    )
    private String path;

    @Schema(
            description = "List of validation errors (only present for validation failures)"
    )
    private List<ValidationError> validationErrors;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "Field-level validation error details")
    public static class ValidationError {

        @Schema(
                description = "Field that failed validation",
                example = "email"
        )
        private String field;

        @Schema(
                description = "Validation error message",
                example = "Email must be valid"
        )
        private String message;
    }
}
