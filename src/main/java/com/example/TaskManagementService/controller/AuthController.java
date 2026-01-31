package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.dto.AuthResponse;
import com.example.TaskManagementService.dto.LoginRequest;
import com.example.TaskManagementService.dto.RegisterRequest;
import com.example.TaskManagementService.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("HTTP POST /api/auth/register received for email={}", request.getEmail());

        AuthResponse response = authService.register(request);

        log.info("HTTP POST /api/auth/register successful for email={}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("HTTP POST /api/auth/login attempt for email={}", request.getEmail());

        AuthResponse response = authService.login(request);

        log.info("HTTP POST /api/auth/login successful for email={}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}
