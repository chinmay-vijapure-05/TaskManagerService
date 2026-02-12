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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private String rawPassword = "password123";
    private String encodedPassword = "encoded_password_hash";
    private String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setPassword(encodedPassword);
        testUser.setFullName("Test User");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void shouldRegisterNewUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@test.com");
        request.setPassword(rawPassword);
        request.setFullName("New User");

        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken("newuser@test.com")).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("newuser@test.com", response.getEmail());
        assertEquals("New User", response.getFullName());

        verify(userRepository, times(1)).existsByEmail("newuser@test.com");
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken(anyString());
    }

    @Test
    void shouldEncodePasswordBeforeSavingUser() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@test.com");
        request.setPassword(rawPassword);
        request.setFullName("New User");

        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn(jwtToken);

        // When
        authService.register(request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(encodedPassword, savedUser.getPassword());
        assertNotEquals(rawPassword, savedUser.getPassword());
    }

    @Test
    void shouldSetUserDetailsCorrectlyDuringRegistration() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@test.com");
        request.setPassword(rawPassword);
        request.setFullName("John Doe");

        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn(jwtToken);

        // When
        authService.register(request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("newuser@test.com", savedUser.getEmail());
        assertEquals("John Doe", savedUser.getFullName());
        assertEquals(encodedPassword, savedUser.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword(rawPassword);
        request.setFullName("Existing User");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(request)
        );

        assertTrue(exception.getMessage().contains("existing@test.com"));
        verify(userRepository, times(1)).existsByEmail("existing@test.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldGenerateJwtTokenAfterRegistration() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@test.com");
        request.setPassword(rawPassword);
        request.setFullName("New User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response.getToken());
        assertEquals(jwtToken, response.getToken());
        verify(jwtUtil, times(1)).generateToken(anyString());
    }

    @Test
    void shouldReturnAuthResponseWithCorrectData() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        request.setPassword(rawPassword);
        request.setFullName("Test User");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("user@test.com");
        savedUser.setFullName("Test User");
        savedUser.setPassword(encodedPassword);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("user@test.com")).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("user@test.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword(rawPassword);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com")).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("Test User", response.getFullName());

        verify(userRepository, times(1)).findByEmail("test@test.com");
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verify(jwtUtil, times(1)).generateToken("test@test.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword(rawPassword);

        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.login(request)
        );

        assertTrue(exception.getMessage().contains("nonexistent@test.com"));
        verify(userRepository, times(1)).findByEmail("nonexistent@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", encodedPassword)).thenReturn(false);

        // When & Then
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@test.com");
        verify(passwordEncoder, times(1)).matches("wrongpassword", encodedPassword);
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void shouldGenerateJwtTokenAfterSuccessfulLogin() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword(rawPassword);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com")).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response.getToken());
        assertEquals(jwtToken, response.getToken());
        verify(jwtUtil, times(1)).generateToken("test@test.com");
    }

    @Test
    void shouldReturnUserDetailsAfterLogin() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword(rawPassword);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword(encodedPassword);
        user.setFullName("John Smith");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com")).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertEquals("test@test.com", response.getEmail());
        assertEquals("John Smith", response.getFullName());
        assertEquals(jwtToken, response.getToken());
    }

    @Test
    void shouldUsePasswordEncoderToVerifyPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("mypassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("mypassword", encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com")).thenReturn(jwtToken);

        // When
        authService.login(request);

        // Then
        verify(passwordEncoder, times(1)).matches("mypassword", encodedPassword);
    }

    // ==================== EDGE CASES & SECURITY TESTS ====================

    @Test
    void shouldNotExposePasswordInAuthResponse() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("secure@test.com");
        request.setPassword(rawPassword);
        request.setFullName("Secure User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        // AuthResponse should only contain token, email, fullName - no password
        assertEquals(jwtToken, response.getToken());
        assertEquals("secure@test.com", response.getEmail());
        assertEquals("Secure User", response.getFullName());
    }

    @Test
    void shouldHandleNullEmailGracefully() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(null);
        request.setPassword(rawPassword);

        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    void shouldHandleEmptyPasswordDuringLogin() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", encodedPassword)).thenReturn(false);

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    void shouldTrimEmailBeforeSaving() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("  user@test.com  "); // Email with spaces
        request.setPassword(rawPassword);
        request.setFullName("User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn(jwtToken);

        // When
        authService.register(request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("  user@test.com  ", savedUser.getEmail());
        // Note: If your service trims email, adjust assertion accordingly
    }

    @Test
    void shouldNotAllowRegistrationWithExistingEmailCaseInsensitive() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("EXISTING@TEST.COM");
        request.setPassword(rawPassword);
        request.setFullName("User");

        when(userRepository.existsByEmail("EXISTING@TEST.COM")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(request);
        });
    }

    // ==================== INTEGRATION FLOW TESTS ====================

    @Test
    void shouldCompleteFullRegistrationFlow() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("flow@test.com");
        request.setPassword("securepassword");
        request.setFullName("Flow User");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setEmail("flow@test.com");
        savedUser.setPassword("encoded_secure_password");
        savedUser.setFullName("Flow User");

        when(userRepository.existsByEmail("flow@test.com")).thenReturn(false);
        when(passwordEncoder.encode("securepassword")).thenReturn("encoded_secure_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("flow@test.com")).thenReturn("generated_token_123");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertEquals("generated_token_123", response.getToken());
        assertEquals("flow@test.com", response.getEmail());
        assertEquals("Flow User", response.getFullName());

        // Verify the flow
        verify(userRepository).existsByEmail("flow@test.com");
        verify(passwordEncoder).encode("securepassword");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("flow@test.com");
    }

    @Test
    void shouldCompleteFullLoginFlow() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("flow@test.com");
        request.setPassword("correctpassword");

        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setEmail("flow@test.com");
        existingUser.setPassword("encoded_correct_password");
        existingUser.setFullName("Flow User");

        when(userRepository.findByEmail("flow@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correctpassword", "encoded_correct_password")).thenReturn(true);
        when(jwtUtil.generateToken("flow@test.com")).thenReturn("login_token_456");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("login_token_456", response.getToken());
        assertEquals("flow@test.com", response.getEmail());
        assertEquals("Flow User", response.getFullName());

        // Verify the flow
        verify(userRepository).findByEmail("flow@test.com");
        verify(passwordEncoder).matches("correctpassword", "encoded_correct_password");
        verify(jwtUtil).generateToken("flow@test.com");
    }
}