package com.sportsaas.auth.domain.entity;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Role entity representing a named set of permissions.
 * Roles can be tenant-specific or system-level.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends TenantAwareEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "is_system_role")
    @Builder.Default
    private Boolean isSystemRole = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Checks if this role has a specific permission.
     */
    public boolean hasPermission(String permissionName) {
        return permissions.stream()
            .anyMatch(p -> p.getName().equals(permissionName));
    }

    /**
     * Checks if this role has permission for a specific resource and action.
     */
    public boolean hasPermission(String resource, String action) {
        return permissions.stream()
            .anyMatch(p -> p.getResource().equals(resource) && p.getAction().equals(action));
    }
}
