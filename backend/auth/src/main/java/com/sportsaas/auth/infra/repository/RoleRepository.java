package com.sportsaas.auth.infra.repository;

import com.sportsaas.auth.domain.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndTenantId(String name, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);

    Page<Role> findByTenantId(UUID tenantId, Pageable pageable);

    List<Role> findByTenantIdOrIsSystemRoleTrue(UUID tenantId);

    List<Role> findByIsSystemRoleTrue();
}
