package com.sportsaas.auth.domain.entity;

import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;
import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing a user in the system.
 * Users are tenant-aware and belong to a specific tenant.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends TenantAwareEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expires_at")
    private LocalDateTime emailVerificationTokenExpiresAt;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * Returns the full name of the user.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Checks if the user account is currently locked.
     */
    public boolean isLocked() {
        if (status == UserStatus.LOCKED) {
            if (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user can log in.
     */
    public boolean canLogin() {
        return status == UserStatus.ACTIVE && !isLocked();
    }

    /**
     * Increments failed login attempts and locks account if threshold exceeded.
     */
    public void incrementFailedLoginAttempts() {
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            status = UserStatus.LOCKED;
            lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Resets failed login attempts after successful login.
     */
    public void resetFailedLoginAttempts() {
        failedLoginAttempts = 0;
        lockedUntil = null;
        if (status == UserStatus.LOCKED) {
            status = UserStatus.ACTIVE;
        }
    }
}
