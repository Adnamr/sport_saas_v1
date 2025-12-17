package com.sportsaas.catalog.infra.repository;

import com.sportsaas.catalog.domain.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Category entity.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findBySlugAndTenantId(String slug, UUID tenantId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndTenantId(String slug, UUID tenantId);

    Page<Category> findByTenantId(UUID tenantId, Pageable pageable);

    List<Category> findByTenantIdAndActiveTrue(UUID tenantId);

    List<Category> findByTenantIdAndParentIsNull(UUID tenantId);

    List<Category> findByTenantIdAndParentIsNullAndActiveTrue(UUID tenantId);

    List<Category> findByParentId(UUID parentId);

    List<Category> findByParentIdAndActiveTrue(UUID parentId);

    @Query("SELECT c FROM Category c WHERE c.tenantId = :tenantId AND c.active = true ORDER BY c.sortOrder, c.name")
    List<Category> findAllActiveByTenantIdOrdered(@Param("tenantId") UUID tenantId);

    @Query("SELECT c FROM Category c WHERE c.tenantId = :tenantId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Category> searchByTenantId(@Param("tenantId") UUID tenantId,
                                     @Param("search") String search,
                                     Pageable pageable);

    long countByTenantId(UUID tenantId);

    long countByParentId(UUID parentId);
}
