package com.sportsaas.auth.api.dto;

/**
 * Response DTO for user statistics.
 */
public record UserStatisticsResponse(
    long total,
    long active,
    long pending,
    long suspended,
    long locked,
    long admins,
    long employees,
    long customers
) {}
