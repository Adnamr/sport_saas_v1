package com.sportsaas.auth.api.controller;

import com.sportsaas.auth.api.dto.AuthResponse;
import com.sportsaas.auth.api.dto.ChangePasswordRequest;
import com.sportsaas.auth.api.dto.ForgotPasswordRequest;
import com.sportsaas.auth.api.dto.LoginRequest;
import com.sportsaas.auth.api.dto.RefreshTokenRequest;
import com.sportsaas.auth.api.dto.RegisterRequest;
import com.sportsaas.auth.api.dto.ResetPasswordRequest;
import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.service.AuthService;
import com.sportsaas.auth.domain.service.AuthService.AuthResult;
import com.sportsaas.tenant.domain.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Authentication REST controller.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        AuthResult result = authService.login(request.email(), request.password(), tenantId);

        return ResponseEntity.ok(toAuthResponse(result));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        User user = User.builder()
            .email(request.email())
            .password(request.password())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phoneNumber(request.phoneNumber())
            .tenantId(tenantId)
            .role(UserRole.CUSTOMER)
            .build();

        User savedUser = authService.register(user);

        return ResponseEntity.ok(Map.of(
            "message", "Registration successful. Please check your email to verify your account.",
            "userId", savedUser.getId()
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResult result = authService.refreshToken(request.refreshToken());

        return ResponseEntity.ok(toAuthResponse(result));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout user and revoke refresh token")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logoutSession(request.refreshToken());

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Initiate password reset flow")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.initiatePasswordReset(request.email());

        return ResponseEntity.ok(Map.of(
            "message", "If an account exists with that email, a password reset link has been sent."
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password with token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with token")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);

        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    private AuthResponse toAuthResponse(AuthResult result) {
        User user = result.user();
        return new AuthResponse(
            result.accessToken(),
            result.refreshToken(),
            result.expiresIn(),
            new AuthResponse.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getTenantId()
            )
        );
    }
}
