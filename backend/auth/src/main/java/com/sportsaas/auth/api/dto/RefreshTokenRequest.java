package com.sportsaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request DTO.
 */
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}
