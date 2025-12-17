package com.sportsaas.tenant.infra.service;

import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.ForbiddenException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.tenant.domain.entity.Tenant;
import com.sportsaas.tenant.domain.enums.SubscriptionPlan;
import com.sportsaas.tenant.domain.enums.TenantStatus;
import com.sportsaas.tenant.infra.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TenantServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantServiceImpl tenantService;

    private Tenant testTenant;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        testTenant = Tenant.builder()
            .name("Test Company")
            .slug("test-company")
            .contactEmail("contact@test.com")
            .status(TenantStatus.ACTIVE)
            .subscriptionPlan(SubscriptionPlan.BASIC)
            .build();
    }

    @Test
    void shouldCreateTenant() {
        // Given
        when(tenantRepository.existsBySlug("test-company")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // When
        Tenant result = tenantService.create(testTenant);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Company");
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void shouldThrowExceptionWhenSlugExists() {
        // Given
        when(tenantRepository.existsBySlug("test-company")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> tenantService.create(testTenant))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldFindTenantById() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));

        // When
        Optional<Tenant> result = tenantService.findById(tenantId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Company");
    }

    @Test
    void shouldFindTenantBySlug() {
        // Given
        when(tenantRepository.findBySlug("test-company")).thenReturn(Optional.of(testTenant));

        // When
        Optional<Tenant> result = tenantService.findBySlug("test-company");

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void shouldFindAllTenants() {
        // Given
        Page<Tenant> page = new PageImpl<>(List.of(testTenant));
        when(tenantRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When
        Page<Tenant> result = tenantService.findAll(PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldCheckSlugAvailability() {
        // Given
        when(tenantRepository.existsBySlug("new-slug")).thenReturn(false);
        when(tenantRepository.existsBySlug("existing-slug")).thenReturn(true);

        // When/Then
        assertThat(tenantService.isSlugAvailable("new-slug")).isTrue();
        assertThat(tenantService.isSlugAvailable("existing-slug")).isFalse();
    }

    @Test
    void shouldActivateTenant() {
        // Given
        testTenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // When
        Tenant result = tenantService.activate(tenantId);

        // Then
        assertThat(result.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void shouldSuspendTenant() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // When
        Tenant result = tenantService.suspend(tenantId);

        // Then
        assertThat(result.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
    }

    @Test
    void shouldThrowNotFoundWhenActivatingNonExistentTenant() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tenantService.activate(tenantId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldValidateTenantAccess() {
        // Given
        testTenant.setStatus(TenantStatus.ACTIVE);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));

        // When/Then - should not throw
        tenantService.validateTenantAccess(tenantId);
    }

    @Test
    void shouldThrowForbiddenForSuspendedTenant() {
        // Given
        testTenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));

        // When/Then
        assertThatThrownBy(() -> tenantService.validateTenantAccess(tenantId))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("not active");
    }

    @Test
    void shouldThrowForbiddenForExpiredTrial() {
        // Given
        testTenant.setStatus(TenantStatus.TRIAL);
        testTenant.setTrialEndsAt(LocalDateTime.now().minusDays(1));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));

        // When/Then
        assertThatThrownBy(() -> tenantService.validateTenantAccess(tenantId))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("expired");
    }
}
