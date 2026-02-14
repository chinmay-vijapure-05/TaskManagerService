package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.dto.AuthResponse;
import com.example.TaskManagementService.dto.LoginRequest;
import com.example.TaskManagementService.dto.RegisterRequest;
import com.example.TaskManagementService.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns a JWT token upon successful registration."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed"),
            @ApiResponse(responseCode = "409",
                    description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request received for email={}", request.getEmail());

        AuthResponse response = authService.register(request);

        log.info("User registered successfully for email={}", request.getEmail());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates user credentials and returns a JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Invalid credentials"),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login attempt received for email={}", request.getEmail());

        AuthResponse response = authService.login(request);

        log.info("Login successful for email={}", request.getEmail());

        return ResponseEntity.ok(response);
    }
}
