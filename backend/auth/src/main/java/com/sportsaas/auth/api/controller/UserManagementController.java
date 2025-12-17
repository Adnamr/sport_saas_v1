package com.sportsaas.auth.api.controller;

import com.sportsaas.auth.api.dto.AdminUpdateUserRequest;
import com.sportsaas.auth.api.dto.AssignRoleRequest;
import com.sportsaas.auth.api.dto.CreateUserRequest;
import com.sportsaas.auth.api.dto.UserResponse;
import com.sportsaas.auth.api.dto.UserStatisticsResponse;
import com.sportsaas.auth.api.mapper.UserMapper;
import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;
import com.sportsaas.auth.domain.service.UserService;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.tenant.domain.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Admin REST controller for managing users.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin endpoints for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserManagementController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List users", description = "Get paginated list of users in the tenant")
    public Page<UserResponse> listUsers(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return userService.findByTenantId(tenantId, pageable)
            .map(userMapper::toResponse);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Search users", description = "Search users by name or email")
    public Page<UserResponse> searchUsers(
            @RequestParam String query,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return userService.searchByTenantId(tenantId, query, pageable)
            .map(userMapper::toResponse);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List users by status", description = "Get users filtered by status")
    public Page<UserResponse> listUsersByStatus(
            @PathVariable UserStatus status,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return userService.findByTenantIdAndStatus(tenantId, status, pageable)
            .map(userMapper::toResponse);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List users by role", description = "Get users filtered by role")
    public Page<UserResponse> listUsersByRole(
            @PathVariable UserRole role,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return userService.findByTenantIdAndRole(tenantId, role, pageable)
            .map(userMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get user", description = "Get a specific user by ID")
    public UserResponse getUser(@PathVariable UUID id) {
        User user = userService.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return userMapper.toResponse(user);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user", description = "Create a new user in the tenant")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        User user = User.builder()
            .tenantId(tenantId)
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phoneNumber(request.phoneNumber())
            .role(request.role())
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .build();

        User created = userService.create(user);
        return userMapper.toResponse(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update user", description = "Update a user's details")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        User userUpdate = User.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phoneNumber(request.phoneNumber())
            .avatarUrl(request.avatarUrl())
            .role(request.role())
            .status(request.status())
            .build();

        User updated = userService.adminUpdate(id, userUpdate);
        return userMapper.toResponse(updated);
    }

    @PostMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Assign role", description = "Assign a role to a user")
    public UserResponse assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest request) {
        User updated = userService.assignRole(id, request.role());
        return userMapper.toResponse(updated);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Activate user", description = "Activate a user account")
    public UserResponse activateUser(@PathVariable UUID id) {
        User activated = userService.activate(id);
        return userMapper.toResponse(activated);
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Suspend user", description = "Suspend a user account")
    public UserResponse suspendUser(@PathVariable UUID id) {
        User suspended = userService.suspend(id);
        return userMapper.toResponse(suspended);
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Unlock user", description = "Unlock a locked user account")
    public UserResponse unlockUser(@PathVariable UUID id) {
        User unlocked = userService.unlock(id);
        return userMapper.toResponse(unlocked);
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Reset password", description = "Reset a user's password")
    public UserResponse resetPassword(
            @PathVariable UUID id,
            @RequestParam String newPassword) {
        User updated = userService.resetPassword(id, passwordEncoder.encode(newPassword));
        return userMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user", description = "Delete a user account")
    public void deleteUser(@PathVariable UUID id) {
        userService.delete(id);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get statistics", description = "Get user statistics for the tenant")
    public UserStatisticsResponse getStatistics() {
        UUID tenantId = TenantContext.requireTenantId();
        return userService.getStatistics(tenantId);
    }
}
