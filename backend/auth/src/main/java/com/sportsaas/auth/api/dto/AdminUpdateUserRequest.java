package com.sportsaas.auth.api.dto;

import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;

/**
 * Admin request DTO for updating a user.
 */
public record AdminUpdateUserRequest(
    String firstName,
    String lastName,
    String phoneNumber,
    String avatarUrl,
    UserRole role,
    UserStatus status
) {}
