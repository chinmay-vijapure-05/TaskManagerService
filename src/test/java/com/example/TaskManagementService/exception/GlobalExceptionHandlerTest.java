package com.example.TaskManagementService.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -----------------------
    // Dummy controller
    // -----------------------

    @RestController
    static class TestController {

        @GetMapping("/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("Resource missing");
        }

        @GetMapping("/unauthorized")
        public void unauthorized() {
            throw new UnauthorizedException("Access denied");
        }

        @GetMapping("/bad-request")
        public void badRequest() {
            throw new BadRequestException("Bad input");
        }

        @GetMapping("/duplicate")
        public void duplicate() {
            throw new DuplicateResourceException("Already exists");
        }

        @GetMapping("/auth-error")
        public void authError() {
            throw new BadCredentialsException("Wrong password");
        }

        @GetMapping("/generic-error")
        public void genericError() {
            throw new RuntimeException("Unexpected");
        }

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody TestRequest request) {
        }
    }

    static class TestRequest {
        @NotBlank(message = "Name must not be blank")
        public String name;
    }

    // -----------------------
    // Tests
    // -----------------------

    @Test
    void shouldHandleResourceNotFound() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource missing"))
                .andExpect(jsonPath("$.path").value("/not-found"));
    }

    @Test
    void shouldHandleUnauthorizedException() throws Exception {
        mockMvc.perform(get("/unauthorized"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void shouldHandleBadRequestException() throws Exception {
        mockMvc.perform(get("/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void shouldHandleDuplicateResourceException() throws Exception {
        mockMvc.perform(get("/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void shouldHandleAuthenticationException() throws Exception {
        mockMvc.perform(get("/auth-error"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors", notNullValue()))
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"));
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
