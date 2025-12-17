package com.sportsaas.auth.domain.service;

import com.sportsaas.auth.api.dto.UserStatisticsResponse;
import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for User operations.
 */
public interface UserService {

    User create(User user);

    User update(UUID id, User user);

    User adminUpdate(UUID id, User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    Page<User> findByTenantId(UUID tenantId, Pageable pageable);

    Page<User> findByTenantIdAndStatus(UUID tenantId, UserStatus status, Pageable pageable);

    Page<User> findByTenantIdAndRole(UUID tenantId, UserRole role, Pageable pageable);

    Page<User> searchByTenantId(UUID tenantId, String search, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    User activate(UUID id);

    User suspend(UUID id);

    User unlock(UUID id);

    User assignRole(UUID id, UserRole role);

    User resetPassword(UUID id, String encodedPassword);

    void delete(UUID id);

    long countByTenantId(UUID tenantId);

    UserStatisticsResponse getStatistics(UUID tenantId);

    void updateLastLogin(UUID userId);

    void incrementFailedLoginAttempts(UUID userId);

    void resetFailedLoginAttempts(UUID userId);
}
