package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.AuthResponse;
import com.example.TaskManagementService.dto.LoginRequest;
import com.example.TaskManagementService.dto.RegisterRequest;
import com.example.TaskManagementService.entity.User;
import com.example.TaskManagementService.exception.BadRequestException;
import com.example.TaskManagementService.exception.DuplicateResourceException;
import com.example.TaskManagementService.exception.ResourceNotFoundException;
import com.example.TaskManagementService.repository.UserRepository;
import com.example.TaskManagementService.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        log.info("Register request received for email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists -> {}", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());

        userRepository.save(user);
        log.info("User registered successfully with email={}", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail());
        log.debug("JWT generated successfully for email={}", user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for email={}", request.getEmail());
                    return new ResourceNotFoundException("User", "email", request.getEmail());
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid credentials for email={}", request.getEmail());
            throw new BadRequestException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("Login successful for email={}", user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }
}
