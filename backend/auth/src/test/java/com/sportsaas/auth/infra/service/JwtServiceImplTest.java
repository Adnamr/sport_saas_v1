package com.sportsaas.auth.infra.service;

import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtServiceImpl.
 */
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtService, "secret",
            "dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IGZvciBqd3QgdG9rZW4gc2lnbmluZyBpbiBkZXZlbG9wbWVudA==");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);

        testUser = User.builder()
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .password("password")
            .status(UserStatus.ACTIVE)
            .role(UserRole.CUSTOMER)
            .tenantId(UUID.randomUUID())
            .build();
        // Set ID manually for testing
        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());
    }

    @Test
    void shouldGenerateAccessToken() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldGenerateRefreshToken() {
        // When
        String token = jwtService.generateRefreshToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldValidateToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        // When
        boolean isValid = jwtService.validateToken("invalid.token.here");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldExtractUserId() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        UUID userId = jwtService.extractUserId(token);

        // Then
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    void shouldExtractEmail() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String email = jwtService.extractEmail(token);

        // Then
        assertThat(email).isEqualTo(testUser.getEmail());
    }

    @Test
    void shouldExtractTenantId() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        UUID tenantId = jwtService.extractTenantId(token);

        // Then
        assertThat(tenantId).isEqualTo(testUser.getTenantId());
    }

    @Test
    void shouldDetectNonExpiredToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }
}
