package com.example.TaskManagementService.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String SECRET =
            "my-very-secure-test-secret-key-which-is-long-enough-123456";

    private final Long EXPIRATION = 1000L * 60; // 1 minute

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Inject private fields manually
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        String token = jwtUtil.generateToken("test@test.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractEmailFromToken() {
        String email = "user@test.com";
        String token = jwtUtil.generateToken(email);

        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtUtil.generateToken("valid@test.com");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void shouldReturnFalseForTamperedToken() {
        String token = jwtUtil.generateToken("test@test.com");

        // Tamper the token
        String tamperedToken = token + "invalid";

        assertFalse(jwtUtil.validateToken(tamperedToken));
    }

    @Test
    void shouldReturnFalseForExpiredToken() throws InterruptedException {
        // Set very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);

        String token = jwtUtil.generateToken("expired@test.com");

        // Wait to expire
        Thread.sleep(5);

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void shouldThrowExceptionWhenExtractingFromInvalidToken() {
        assertThrows(Exception.class, () ->
                jwtUtil.extractEmail("invalid.token.value"));
    }
}
