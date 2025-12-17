package com.sportsaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Reset password request DTO.
 */
public record ResetPasswordRequest(
    @NotBlank(message = "Token is required")
    String token,

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword
) {}
