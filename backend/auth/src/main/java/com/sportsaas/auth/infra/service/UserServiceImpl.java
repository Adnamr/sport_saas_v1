package com.sportsaas.auth.infra.service;

import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserStatus;
import com.sportsaas.auth.domain.service.UserService;
import com.sportsaas.auth.infra.repository.UserRepository;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User with email " + user.getEmail() + " already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(UUID id, User user) {
        User existing = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setPhoneNumber(user.getPhoneNumber());
        existing.setAvatarUrl(user.getAvatarUrl());

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

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User suspend(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setStatus(UserStatus.SUSPENDED);

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

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return userRepository.countByTenantId(tenantId);
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
