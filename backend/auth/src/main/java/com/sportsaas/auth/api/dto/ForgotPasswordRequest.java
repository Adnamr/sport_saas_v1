package com.sportsaas.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Forgot password request DTO.
 */
public record ForgotPasswordRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email
) {}
