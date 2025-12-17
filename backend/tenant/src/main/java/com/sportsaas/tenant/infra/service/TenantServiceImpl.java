package com.sportsaas.tenant.infra.service;

import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.ForbiddenException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.tenant.domain.entity.Tenant;
import com.sportsaas.tenant.domain.enums.TenantStatus;
import com.sportsaas.tenant.domain.service.TenantService;
import com.sportsaas.tenant.infra.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of TenantService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public Tenant create(Tenant tenant) {
        log.info("Creating tenant: {}", tenant.getSlug());

        if (tenantRepository.existsBySlug(tenant.getSlug())) {
            throw new ConflictException("Tenant with slug '" + tenant.getSlug() + "' already exists");
        }

        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant created: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Tenant update(UUID id, Tenant tenant) {
        log.info("Updating tenant: {}", id);

        Tenant existing = findByIdOrThrow(id);

        // Check slug uniqueness if changed
        if (!existing.getSlug().equals(tenant.getSlug())
            && tenantRepository.existsBySlug(tenant.getSlug())) {
            throw new ConflictException("Tenant with slug '" + tenant.getSlug() + "' already exists");
        }

        existing.setName(tenant.getName());
        existing.setSlug(tenant.getSlug());
        existing.setContactEmail(tenant.getContactEmail());
        existing.setContactPhone(tenant.getContactPhone());
        existing.setAddress(tenant.getAddress());
        existing.setLogoUrl(tenant.getLogoUrl());
        existing.setSubscriptionPlan(tenant.getSubscriptionPlan());
        existing.setMaxUsers(tenant.getMaxUsers());
        existing.setMaxProducts(tenant.getMaxProducts());

        Tenant saved = tenantRepository.save(existing);
        log.info("Tenant updated: {}", saved.getId());
        return saved;
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        return tenantRepository.findById(id);
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return tenantRepository.findBySlug(slug);
    }

    @Override
    public Page<Tenant> findAll(Pageable pageable) {
        return tenantRepository.findAll(pageable);
    }

    @Override
    public boolean isSlugAvailable(String slug) {
        return !tenantRepository.existsBySlug(slug);
    }

    @Override
    @Transactional
    public Tenant activate(UUID id) {
        log.info("Activating tenant: {}", id);

        Tenant tenant = findByIdOrThrow(id);
        tenant.setStatus(TenantStatus.ACTIVE);

        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant activated: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Tenant suspend(UUID id) {
        log.info("Suspending tenant: {}", id);

        Tenant tenant = findByIdOrThrow(id);
        tenant.setStatus(TenantStatus.SUSPENDED);

        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant suspended: {}", saved.getId());
        return saved;
    }

    @Override
    public void validateTenantAccess(UUID tenantId) {
        Tenant tenant = findByIdOrThrow(tenantId);

        if (!tenant.isActive()) {
            throw new ForbiddenException("Tenant is not active: " + tenant.getStatus());
        }

        if (tenant.isTrialExpired()) {
            throw new ForbiddenException("Tenant trial has expired");
        }
    }

    private Tenant findByIdOrThrow(UUID id) {
        return tenantRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Tenant", id));
    }
}
