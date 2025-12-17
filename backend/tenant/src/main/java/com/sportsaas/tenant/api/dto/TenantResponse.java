package com.sportsaas.tenant.api.dto;

import com.sportsaas.tenant.domain.enums.SubscriptionPlan;
import com.sportsaas.tenant.domain.enums.TenantStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for tenant data.
 */
public record TenantResponse(
    UUID id,
    String name,
    String slug,
    TenantStatus status,
    SubscriptionPlan subscriptionPlan,
    String contactEmail,
    String contactPhone,
    String address,
    String logoUrl,
    LocalDateTime trialEndsAt,
    LocalDateTime subscriptionEndsAt,
    Integer maxUsers,
    Integer maxProducts,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
