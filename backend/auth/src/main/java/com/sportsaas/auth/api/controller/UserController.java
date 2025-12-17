package com.sportsaas.auth.api.controller;

import com.sportsaas.auth.api.dto.ChangePasswordRequest;
import com.sportsaas.auth.api.dto.UpdateProfileRequest;
import com.sportsaas.auth.api.dto.UserResponse;
import com.sportsaas.auth.api.mapper.UserMapper;
import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.service.AuthService;
import com.sportsaas.auth.domain.service.UserService;
import com.sportsaas.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * User profile REST controller.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get the current authenticated user's profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = getCurrentAuthenticatedUser();
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile", description = "Update the current user's profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = getCurrentAuthenticatedUser();

        User userUpdate = User.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phoneNumber(request.phoneNumber())
            .avatarUrl(request.avatarUrl())
            .build();

        User updated = userService.update(currentUser.getId(), userUpdate);

        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change password", description = "Change the current user's password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = getCurrentAuthenticatedUser();

        authService.changePassword(currentUser.getId(), request.currentPassword(), request.newPassword());

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/me/resend-verification")
    @Operation(summary = "Resend email verification", description = "Resend the email verification link")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> resendVerification() {
        User currentUser = getCurrentAuthenticatedUser();

        authService.resendEmailVerification(currentUser.getId());

        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
