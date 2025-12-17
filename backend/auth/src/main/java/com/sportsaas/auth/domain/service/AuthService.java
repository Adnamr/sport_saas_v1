package com.sportsaas.auth.domain.service;

import com.sportsaas.auth.domain.entity.User;

import java.util.UUID;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticates a user with email and password.
     */
    AuthResult login(String email, String password, UUID tenantId);

    /**
     * Registers a new user.
     */
    User register(User user);

    /**
     * Refreshes an access token using a refresh token.
     */
    AuthResult refreshToken(String refreshToken);

    /**
     * Logs out a user by revoking all refresh tokens.
     */
    void logout(UUID userId);

    /**
     * Logs out a user from a specific session.
     */
    void logoutSession(String refreshToken);

    /**
     * Initiates password reset flow.
     */
    void initiatePasswordReset(String email);

    /**
     * Completes password reset with token.
     */
    void resetPassword(String token, String newPassword);

    /**
     * Changes user password.
     */
    void changePassword(UUID userId, String currentPassword, String newPassword);

    /**
     * Verifies user email.
     */
    User verifyEmail(String token);

    /**
     * Resends email verification.
     */
    void resendEmailVerification(UUID userId);

    /**
     * Authentication result containing tokens and user info.
     */
    record AuthResult(
        String accessToken,
        String refreshToken,
        User user,
        long expiresIn
    ) {}
}
