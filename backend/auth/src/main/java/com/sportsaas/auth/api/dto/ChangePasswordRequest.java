package com.sportsaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Change password request DTO.
 */
public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword
) {}
