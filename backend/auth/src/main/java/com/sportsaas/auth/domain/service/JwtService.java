package com.sportsaas.auth.domain.service;

import com.sportsaas.auth.domain.entity.User;

import java.util.UUID;

/**
 * Service interface for JWT operations.
 */
public interface JwtService {

    /**
     * Generates an access token for the given user.
     */
    String generateAccessToken(User user);

    /**
     * Generates a refresh token for the given user.
     */
    String generateRefreshToken(User user);

    /**
     * Validates the given token.
     */
    boolean validateToken(String token);

    /**
     * Extracts the user ID from the token.
     */
    UUID extractUserId(String token);

    /**
     * Extracts the email from the token.
     */
    String extractEmail(String token);

    /**
     * Extracts the tenant ID from the token.
     */
    UUID extractTenantId(String token);

    /**
     * Checks if the token is expired.
     */
    boolean isTokenExpired(String token);
}
