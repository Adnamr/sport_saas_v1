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
import com.sportsaas.tenant.domain.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserManagementController.
 */
@ExtendWith(MockitoExtension.class)
class UserManagementControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementController controller;

    private MockedStatic<TenantContext> tenantContextMock;
    private UUID tenantId;
    private UUID userId;
    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testUser = User.builder()
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .role(UserRole.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .tenantId(tenantId)
            .build();

        testUserResponse = new UserResponse(
            userId,
            "test@example.com",
            "John",
            "Doe",
            null,
            UserStatus.ACTIVE,
            UserRole.CUSTOMER,
            true,
            null,
            null,
            tenantId,
            null,
            null
        );

        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::requireTenantId).thenReturn(tenantId);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void shouldListUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userService.findByTenantId(tenantId, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        Page<UserResponse> result = controller.listUsers(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("test@example.com");
    }

    @Test
    void shouldSearchUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userService.searchByTenantId(tenantId, "John", pageable)).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        Page<UserResponse> result = controller.searchUsers("John", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldListUsersByStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userService.findByTenantIdAndStatus(tenantId, UserStatus.ACTIVE, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        Page<UserResponse> result = controller.listUsersByStatus(UserStatus.ACTIVE, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldListUsersByRole() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userService.findByTenantIdAndRole(tenantId, UserRole.CUSTOMER, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        Page<UserResponse> result = controller.listUsersByRole(UserRole.CUSTOMER, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldGetUser() {
        // Given
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = controller.getUser(userId);

        // Then
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    void shouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
            "new@example.com",
            "password123",
            "Jane",
            "Smith",
            null,
            UserRole.EMPLOYEE,
            false
        );
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userService.create(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = controller.createUser(request);

        // Then
        assertThat(result).isNotNull();
        verify(userService).create(any(User.class));
    }

    @Test
    void shouldUpdateUser() {
        // Given
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(
            "Jane",
            "Smith",
            "+1234567890",
            null,
            UserRole.EMPLOYEE,
            UserStatus.ACTIVE
        );
        when(userService.adminUpdate(eq(userId), any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = controller.updateUser(userId, request);

        // Then
        assertThat(result).isNotNull();
        verify(userService).adminUpdate(eq(userId), any(User.class));
    }

    @Test
    void shouldAssignRole() {
        // Given
        AssignRoleRequest request = new AssignRoleRequest(UserRole.EMPLOYEE);
        when(userService.assignRole(userId, UserRole.EMPLOYEE)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = controller.assignRole(userId, request);

        // Then
        assertThat(result).isNotNull();
        verify(userService).assignRole(userId, UserRole.EMPLOYEE);
    }

    @Test
    void shouldActivateUser() {
        // Given
        when(userService.activate(userId)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = controller.activateUser(userId);

        // Then
        assertThat(result).isNotNull();
        verify(userService).activate(userId);
    }

    @Test
    void shouldSuspendUser() {
        // Given
        when(userService.suspend(userId)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = controller.suspendUser(userId);

        // Then
        assertThat(result).isNotNull();
        verify(userService).suspend(userId);
    }

    @Test
    void shouldUnlockUser() {
        // Given
        when(userService.unlock(userId)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = controller.unlockUser(userId);

        // Then
        assertThat(result).isNotNull();
        verify(userService).unlock(userId);
    }

    @Test
    void shouldResetPassword() {
        // Given
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded_new_password");
        when(userService.resetPassword(userId, "encoded_new_password")).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = controller.resetPassword(userId, "newPassword123");

        // Then
        assertThat(result).isNotNull();
        verify(userService).resetPassword(userId, "encoded_new_password");
    }

    @Test
    void shouldDeleteUser() {
        // When
        controller.deleteUser(userId);

        // Then
        verify(userService).delete(userId);
    }

    @Test
    void shouldGetStatistics() {
        // Given
        UserStatisticsResponse stats = new UserStatisticsResponse(
            100L, 80L, 10L, 5L, 5L, 2L, 18L, 80L
        );
        when(userService.getStatistics(tenantId)).thenReturn(stats);

        // When
        UserStatisticsResponse result = controller.getStatistics();

        // Then
        assertThat(result.total()).isEqualTo(100L);
        assertThat(result.active()).isEqualTo(80L);
    }
}
