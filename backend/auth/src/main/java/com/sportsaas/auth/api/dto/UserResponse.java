package com.sportsaas.auth.api.dto;

import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User response DTO.
 */
public record UserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    UserStatus status,
    UserRole role,
    Boolean emailVerified,
    LocalDateTime lastLoginAt,
    String avatarUrl,
    UUID tenantId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
