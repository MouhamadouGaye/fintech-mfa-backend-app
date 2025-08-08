package com.mgaye.banking_application.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mgaye.banking_application.dto.ErrorResponse;
import com.mgaye.banking_application.dto.request.AuthenticationRequest;
import com.mgaye.banking_application.dto.request.RegistrationRequest;

// # ========== SECURITY TESTING ==========

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    @Test
    void unauthorizedAccess_ToProtectedEndpoint_ShouldReturn401() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                baseUrl + "/user/profile",
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void sqlInjectionAttempt_ShouldBeBlocked() {
        AuthenticationRequest maliciousRequest = AuthenticationRequest.builder()
                .email("'; DROP TABLE users; --")
                .password("password")
                .build();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                maliciousRequest,
                ErrorResponse.class);

        // Should return validation error, not crash
        assertThat(response.getStatusCode()).isIn(
                HttpStatus.BAD_REQUEST,
                HttpStatus.UNAUTHORIZED);
    }

    @Test
    void xssAttempt_ShouldBeSanitized() {
        RegistrationRequest xssRequest = RegistrationRequest.builder()
                .email("test@example.com")
                .password("TestPass123!")
                .firstName("<script>alert('xss')</script>")
                .lastName("Test")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .acceptTerms(true)
                .acceptPrivacy(true)
                .build();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                xssRequest,
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rateLimiting_ShouldBlockExcessiveRequests() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("rate-limit@test.com")
                .password("WrongPassword")
                .build();

        // Make many requests quickly
        for (int i = 0; i < 15; i++) {
            restTemplate.postForEntity(baseUrl + "/auth/login", request, ErrorResponse.class);
        }

        // Next request should be rate limited
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                request,
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}