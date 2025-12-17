package com.sportsaas.tenant.domain.entity;

import com.sportsaas.common.domain.BaseEntity;
import com.sportsaas.tenant.domain.enums.SubscriptionPlan;
import com.sportsaas.tenant.domain.enums.TenantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Represents a tenant (organization/company) in the multi-tenant system.
 * Each tenant has its own isolated data and configuration.
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Tenant extends BaseEntity {

    /**
     * Display name of the tenant/organization.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Unique URL-friendly identifier (e.g., "acme-sports").
     */
    @Column(nullable = false, unique = true)
    private String slug;

    /**
     * Current status of the tenant.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TenantStatus status = TenantStatus.TRIAL;

    /**
     * Current subscription plan.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    /**
     * Primary contact email.
     */
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    /**
     * Contact phone number.
     */
    @Column(name = "contact_phone")
    private String contactPhone;

    /**
     * Business address.
     */
    @Column(columnDefinition = "TEXT")
    private String address;

    /**
     * URL to the tenant's logo.
     */
    @Column(name = "logo_url")
    private String logoUrl;

    /**
     * When the trial period ends.
     */
    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    /**
     * When the current subscription ends.
     */
    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

    /**
     * Maximum number of users allowed.
     */
    @Column(name = "max_users")
    @Builder.Default
    private Integer maxUsers = 5;

    /**
     * Maximum number of products allowed.
     */
    @Column(name = "max_products")
    @Builder.Default
    private Integer maxProducts = 100;

    /**
     * Whether the tenant is active and can be used.
     */
    public boolean isActive() {
        return status == TenantStatus.ACTIVE || status == TenantStatus.TRIAL;
    }

    /**
     * Whether the trial has expired.
     */
    public boolean isTrialExpired() {
        return status == TenantStatus.TRIAL
            && trialEndsAt != null
            && LocalDateTime.now().isAfter(trialEndsAt);
    }

    /**
     * Whether the subscription has expired.
     */
    public boolean isSubscriptionExpired() {
        return subscriptionEndsAt != null
            && LocalDateTime.now().isAfter(subscriptionEndsAt);
    }
}
