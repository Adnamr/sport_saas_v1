package com.sportsaas.auth.infra.service;

import com.sportsaas.auth.api.dto.UserStatisticsResponse;
import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;
import com.sportsaas.auth.domain.service.UserService;
import com.sportsaas.auth.infra.repository.UserRepository;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User with email " + user.getEmail() + " already exists");
        }

        // Only encode if not already encoded (admin creates users with pre-encoded passwords)
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getStatus() == null) {
            user.setStatus(UserStatus.PENDING);
        }

        if (user.getEmailVerified() == null) {
            user.setEmailVerified(false);
        }

        log.info("Creating user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(UUID id, User user) {
        User existing = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (user.getFirstName() != null) {
            existing.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            existing.setLastName(user.getLastName());
        }
        if (user.getPhoneNumber() != null) {
            existing.setPhoneNumber(user.getPhoneNumber());
        }
        if (user.getAvatarUrl() != null) {
            existing.setAvatarUrl(user.getAvatarUrl());
        }

        log.info("Updating user profile: {}", existing.getEmail());
        return userRepository.save(existing);
    }

    @Override
    @Transactional
    public User adminUpdate(UUID id, User user) {
        User existing = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (user.getFirstName() != null) {
            existing.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            existing.setLastName(user.getLastName());
        }
        if (user.getPhoneNumber() != null) {
            existing.setPhoneNumber(user.getPhoneNumber());
        }
        if (user.getAvatarUrl() != null) {
            existing.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getRole() != null) {
            existing.setRole(user.getRole());
        }
        if (user.getStatus() != null) {
            existing.setStatus(user.getStatus());
        }

        log.info("Admin updating user: {}", existing.getEmail());
        return userRepository.save(existing);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByEmailAndTenantId(String email, UUID tenantId) {
        return userRepository.findByEmailAndTenantId(email, tenantId);
    }

    @Override
    public Page<User> findByTenantId(UUID tenantId, Pageable pageable) {
        return userRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public Page<User> findByTenantIdAndStatus(UUID tenantId, UserStatus status, Pageable pageable) {
        return userRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    @Override
    public Page<User> findByTenantIdAndRole(UUID tenantId, UserRole role, Pageable pageable) {
        return userRepository.findByTenantIdAndRole(tenantId, role, pageable);
    }

    @Override
    public Page<User> searchByTenantId(UUID tenantId, String search, Pageable pageable) {
        return userRepository.searchByTenantId(tenantId, search, pageable);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndTenantId(String email, UUID tenantId) {
        return userRepository.existsByEmailAndTenantId(email, tenantId);
    }

    @Override
    @Transactional
    public User activate(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        log.info("Activating user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User suspend(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setStatus(UserStatus.SUSPENDED);

        log.info("Suspending user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User unlock(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        log.info("Unlocking user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User assignRole(UUID id, UserRole role) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setRole(role);

        log.info("Assigning role {} to user: {}", role, user.getEmail());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User resetPassword(UUID id, String encodedPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setPassword(encodedPassword);
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        log.info("Resetting password for user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setStatus(UserStatus.DELETED);
        log.info("Deleting user: {}", user.getEmail());
        userRepository.save(user);
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return userRepository.countByTenantId(tenantId);
    }

    @Override
    public UserStatisticsResponse getStatistics(UUID tenantId) {
        return new UserStatisticsResponse(
            userRepository.countByTenantId(tenantId),
            userRepository.countByTenantIdAndStatus(tenantId, UserStatus.ACTIVE),
            userRepository.countByTenantIdAndStatus(tenantId, UserStatus.PENDING),
            userRepository.countByTenantIdAndStatus(tenantId, UserStatus.SUSPENDED),
            userRepository.countByTenantIdAndStatus(tenantId, UserStatus.LOCKED),
            userRepository.countByTenantIdAndRole(tenantId, UserRole.TENANT_ADMIN),
            userRepository.countByTenantIdAndRole(tenantId, UserRole.EMPLOYEE),
            userRepository.countByTenantIdAndRole(tenantId, UserRole.CUSTOMER)
        );
    }

    @Override
    @Transactional
    public void updateLastLogin(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void incrementFailedLoginAttempts(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        user.incrementFailedLoginAttempts();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetFailedLoginAttempts(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        user.resetFailedLoginAttempts();
        userRepository.save(user);
    }
}
