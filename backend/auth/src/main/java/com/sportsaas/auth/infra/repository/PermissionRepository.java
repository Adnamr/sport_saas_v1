package com.sportsaas.auth.infra.repository;

import com.sportsaas.auth.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission entity.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    List<Permission> findByResource(String resource);

    List<Permission> findByResourceAndAction(String resource, String action);
}
