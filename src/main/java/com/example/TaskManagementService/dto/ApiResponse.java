package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Generic API response wrapper")
public class ApiResponse<T> {

    @Schema(
            description = "Indicates whether the request was successful",
            example = "true"
    )
    private boolean success;

    @Schema(
            description = "Human-readable response message",
            example = "Operation completed successfully"
    )
    private String message;

    @Schema(
            description = "Actual response payload data"
    )
    private T data;

    @Schema(
            description = "Timestamp when the response was generated",
            example = "2026-02-14T17:10:30"
    )
    private LocalDateTime timestamp;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
}
