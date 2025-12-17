package com.sportsaas.tenant.api.dto;

import com.sportsaas.tenant.domain.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a tenant.
 */
public record UpdateTenantRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @NotBlank(message = "Slug is required")
    @Size(min = 2, max = 50, message = "Slug must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
    String slug,

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    String contactEmail,

    String contactPhone,

    String address,

    String logoUrl,

    SubscriptionPlan subscriptionPlan,

    @Min(value = 1, message = "Max users must be at least 1")
    Integer maxUsers,

    @Min(value = 1, message = "Max products must be at least 1")
    Integer maxProducts
) {}
