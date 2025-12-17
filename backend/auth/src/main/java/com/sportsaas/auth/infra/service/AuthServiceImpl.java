package com.sportsaas.auth.infra.service;

import com.sportsaas.auth.domain.entity.RefreshToken;
import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserStatus;
import com.sportsaas.auth.domain.service.AuthService;
import com.sportsaas.auth.domain.service.JwtService;
import com.sportsaas.auth.domain.service.UserService;
import com.sportsaas.auth.infra.repository.RefreshTokenRepository;
import com.sportsaas.auth.infra.repository.UserRepository;
import com.sportsaas.common.exception.BadRequestException;
import com.sportsaas.common.exception.ForbiddenException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of AuthService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthResult login(String email, String password, UUID tenantId) {
        User user = userRepository.findByEmailAndTenantId(email, tenantId)
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.canLogin()) {
            if (user.isLocked()) {
                throw new ForbiddenException("Account is locked. Try again later.");
            }
            throw new ForbiddenException("Account is not active");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            userService.incrementFailedLoginAttempts(user.getId());
            throw new UnauthorizedException("Invalid credentials");
        }

        userService.resetFailedLoginAttempts(user.getId());
        userService.updateLastLogin(user.getId());

        return createAuthResult(user);
    }

    @Override
    @Transactional
    public User register(User user) {
        if (userRepository.existsByEmailAndTenantId(user.getEmail(), user.getTenantId())) {
            throw new BadRequestException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusDays(1));

        User savedUser = userRepository.save(user);

        // TODO: Send verification email via event

        return savedUser;
    }

    @Override
    @Transactional
    public AuthResult refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenStr)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        if (!user.canLogin()) {
            throw new ForbiddenException("Account is not active");
        }

        // Revoke old refresh token
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        return createAuthResult(user);
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        refreshTokenRepository.revokeAllByUser(user, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void logoutSession(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
            .orElseThrow(() -> new NotFoundException("Refresh token not found"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPasswordResetToken(UUID.randomUUID().toString());
            user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            // TODO: Send password reset email via event
        });

        // Don't reveal if email exists
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getPasswordResetTokenExpiresAt() == null ||
            user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByUser(user, LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (user.getEmailVerificationTokenExpiresAt() == null ||
            user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendEmailVerification(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getEmailVerified()) {
            throw new BadRequestException("Email already verified");
        }

        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        // TODO: Send verification email via event
    }

    private AuthResult createAuthResult(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenStr = jwtService.generateRefreshToken(user);

        // Store refresh token
        RefreshToken refreshToken = RefreshToken.builder()
            .token(refreshTokenStr)
            .user(user)
            .tenantId(user.getTenantId())
            .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
            .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResult(
            accessToken,
            refreshTokenStr,
            user,
            accessTokenExpiration / 1000
        );
    }
}
