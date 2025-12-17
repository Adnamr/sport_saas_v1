package com.sportsaas.auth.api.dto;

import com.sportsaas.auth.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for assigning a role to a user.
 */
public record AssignRoleRequest(
    @NotNull(message = "Role is required")
    UserRole role
) {}
