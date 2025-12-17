package com.sportsaas.auth.domain;

import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User entity.
 */
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .email("test@example.com")
            .password("password")
            .firstName("John")
            .lastName("Doe")
            .status(UserStatus.ACTIVE)
            .role(UserRole.CUSTOMER)
            .tenantId(UUID.randomUUID())
            .build();
    }

    @Test
    void shouldReturnFullName() {
        // When
        String fullName = user.getFullName();

        // Then
        assertThat(fullName).isEqualTo("John Doe");
    }

    @Test
    void shouldNotBeLockedWhenStatusIsActive() {
        // Given
        user.setStatus(UserStatus.ACTIVE);

        // When/Then
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    void shouldBeLockedWhenStatusIsLockedAndNotExpired() {
        // Given
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));

        // When/Then
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    void shouldNotBeLockedWhenLockExpired() {
        // Given
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));

        // When/Then
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    void shouldCanLoginWhenActive() {
        // Given
        user.setStatus(UserStatus.ACTIVE);

        // When/Then
        assertThat(user.canLogin()).isTrue();
    }

    @Test
    void shouldNotCanLoginWhenSuspended() {
        // Given
        user.setStatus(UserStatus.SUSPENDED);

        // When/Then
        assertThat(user.canLogin()).isFalse();
    }

    @Test
    void shouldIncrementFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(0);

        // When
        user.incrementFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void shouldLockAccountAfterFiveFailedAttempts() {
        // Given
        user.setFailedLoginAttempts(4);
        user.setStatus(UserStatus.ACTIVE);

        // When
        user.incrementFailedLoginAttempts();

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(user.getLockedUntil()).isNotNull();
    }

    @Test
    void shouldResetFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(3);
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));

        // When
        user.resetFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
