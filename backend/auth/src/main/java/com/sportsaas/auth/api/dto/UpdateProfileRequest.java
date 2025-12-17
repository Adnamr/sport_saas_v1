package com.sportsaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Update profile request DTO.
 */
public record UpdateProfileRequest(
    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    String phoneNumber,

    String avatarUrl
) {}
