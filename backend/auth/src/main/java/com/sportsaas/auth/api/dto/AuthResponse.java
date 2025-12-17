package com.sportsaas.auth.api.dto;

import java.util.UUID;

/**
 * Authentication response DTO.
 */
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserInfo user
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserInfo user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }

    /**
     * User information included in auth response.
     */
    public record UserInfo(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        UUID tenantId
    ) {}
}
