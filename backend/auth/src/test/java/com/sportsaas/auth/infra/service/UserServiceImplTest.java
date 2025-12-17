package com.sportsaas.auth.infra.service;

import com.sportsaas.auth.domain.entity.User;
import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.enums.UserStatus;
import com.sportsaas.auth.infra.repository.UserRepository;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID userId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        testUser = User.builder()
            .email("test@example.com")
            .password("password123")
            .firstName("John")
            .lastName("Doe")
            .status(UserStatus.ACTIVE)
            .role(UserRole.CUSTOMER)
            .tenantId(tenantId)
            .build();
    }

    @Test
    void shouldCreateUser() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.create(testUser);

        // Then
        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.create(testUser))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldFindUserById() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void shouldFindUsersByTenantId() {
        // Given
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findByTenantId(tenantId, PageRequest.of(0, 10))).thenReturn(page);

        // When
        Page<User> result = userService.findByTenantId(tenantId, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldActivateUser() {
        // Given
        testUser.setStatus(UserStatus.PENDING);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.activate(userId);

        // Then
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getEmailVerified()).isTrue();
    }

    @Test
    void shouldSuspendUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.suspend(userId);

        // Then
        assertThat(result.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void shouldUnlockUser() {
        // Given
        testUser.setStatus(UserStatus.LOCKED);
        testUser.setFailedLoginAttempts(5);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.unlock(userId);

        // Then
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getFailedLoginAttempts()).isZero();
    }

    @Test
    void shouldThrowNotFoundWhenActivatingNonExistentUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.activate(userId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldSoftDeleteUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.delete(userId);

        // Then
        assertThat(testUser.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldAdminUpdateUser() {
        // Given
        User updateData = User.builder()
            .firstName("Jane")
            .lastName("Smith")
            .role(UserRole.EMPLOYEE)
            .status(UserStatus.ACTIVE)
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.adminUpdate(userId, updateData);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getFirstName()).isEqualTo("Jane");
        assertThat(testUser.getLastName()).isEqualTo("Smith");
        assertThat(testUser.getRole()).isEqualTo(UserRole.EMPLOYEE);
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldFindUsersByTenantIdAndRole() {
        // Given
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findByTenantIdAndRole(tenantId, UserRole.CUSTOMER, PageRequest.of(0, 10)))
            .thenReturn(page);

        // When
        Page<User> result = userService.findByTenantIdAndRole(tenantId, UserRole.CUSTOMER, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void shouldAssignRole() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.assignRole(userId, UserRole.EMPLOYEE);

        // Then
        assertThat(result.getRole()).isEqualTo(UserRole.EMPLOYEE);
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldResetPassword() {
        // Given
        testUser.setPasswordResetToken("token123");
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.resetPassword(userId, "new_encoded_password");

        // Then
        assertThat(result.getPassword()).isEqualTo("new_encoded_password");
        assertThat(result.getPasswordResetToken()).isNull();
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldGetStatistics() {
        // Given
        when(userRepository.countByTenantId(tenantId)).thenReturn(100L);
        when(userRepository.countByTenantIdAndStatus(tenantId, UserStatus.ACTIVE)).thenReturn(80L);
        when(userRepository.countByTenantIdAndStatus(tenantId, UserStatus.PENDING)).thenReturn(10L);
        when(userRepository.countByTenantIdAndStatus(tenantId, UserStatus.SUSPENDED)).thenReturn(5L);
        when(userRepository.countByTenantIdAndStatus(tenantId, UserStatus.LOCKED)).thenReturn(5L);
        when(userRepository.countByTenantIdAndRole(tenantId, UserRole.TENANT_ADMIN)).thenReturn(2L);
        when(userRepository.countByTenantIdAndRole(tenantId, UserRole.EMPLOYEE)).thenReturn(18L);
        when(userRepository.countByTenantIdAndRole(tenantId, UserRole.CUSTOMER)).thenReturn(80L);

        // When
        var result = userService.getStatistics(tenantId);

        // Then
        assertThat(result.total()).isEqualTo(100L);
        assertThat(result.active()).isEqualTo(80L);
        assertThat(result.pending()).isEqualTo(10L);
        assertThat(result.suspended()).isEqualTo(5L);
        assertThat(result.locked()).isEqualTo(5L);
        assertThat(result.admins()).isEqualTo(2L);
        assertThat(result.employees()).isEqualTo(18L);
        assertThat(result.customers()).isEqualTo(80L);
    }

    @Test
    void shouldThrowNotFoundWhenAssigningRoleToNonExistentUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.assignRole(userId, UserRole.EMPLOYEE))
            .isInstanceOf(NotFoundException.class);
    }
}
