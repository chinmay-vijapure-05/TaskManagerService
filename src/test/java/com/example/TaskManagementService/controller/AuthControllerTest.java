//package com.example.TaskManagementService.controller;
//
//import com.example.TaskManagementService.BaseIntegrationTest;
//import com.example.TaskManagementService.dto.LoginRequest;
//import com.example.TaskManagementService.dto.RegisterRequest;
//import com.example.TaskManagementService.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MvcResult;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.hamcrest.Matchers.*;
//
//class AuthControllerTest extends BaseIntegrationTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @BeforeEach
//    void cleanup() {
//        userRepository.deleteAll();
//    }
//
//    @Test
//    void shouldRegisterNewUser() throws Exception {
//        RegisterRequest request = new RegisterRequest();
//        request.setEmail("newuser@test.com");
//        request.setPassword("password123");
//        request.setFullName("New User");
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token", notNullValue()))
//                .andExpect(jsonPath("$.email", is("newuser@test.com")))
//                .andExpect(jsonPath("$.fullName", is("New User")));
//    }
//
//    @Test
//    void shouldNotRegisterDuplicateEmail() throws Exception {
//        RegisterRequest request = new RegisterRequest();
//        request.setEmail("duplicate@test.com");
//        request.setPassword("password123");
//        request.setFullName("First User");
//
//        // Register first user
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//
//        // Try to register duplicate
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.message", containsString("already exists")));
//    }
//
//    @Test
//    void shouldLoginWithValidCredentials() throws Exception {
//        // First register a user
//        RegisterRequest registerRequest = new RegisterRequest();
//        registerRequest.setEmail("login@test.com");
//        registerRequest.setPassword("password123");
//        registerRequest.setFullName("Login User");
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registerRequest)))
//                .andExpect(status().isOk());
//
//        // Then try to login
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setEmail("login@test.com");
//        loginRequest.setPassword("password123");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token", notNullValue()))
//                .andExpect(jsonPath("$.email", is("login@test.com")));
//    }
//
//    @Test
//    void shouldNotLoginWithInvalidPassword() throws Exception {
//        // Register a user
//        RegisterRequest registerRequest = new RegisterRequest();
//        registerRequest.setEmail("invalid@test.com");
//        registerRequest.setPassword("correctpassword");
//        registerRequest.setFullName("Invalid User");
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registerRequest)))
//                .andExpect(status().isOk());
//
//        // Try to login with wrong password
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setEmail("invalid@test.com");
//        loginRequest.setPassword("wrongpassword");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void shouldValidateRegistrationFields() throws Exception {
//        RegisterRequest request = new RegisterRequest();
//        request.setEmail("invalid-email");  // Invalid email format
//        request.setPassword("123");  // Too short
//        request.setFullName("");  // Empty name
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.validationErrors", notNullValue()));
//    }
//}