package com.sportsaas.catalog.infra.repository;

import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.enums.ProductStatus;
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
 * Repository for Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySlugAndTenantId(String slug, UUID tenantId);

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySkuAndTenantId(String sku, UUID tenantId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndTenantId(String slug, UUID tenantId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndTenantId(String sku, UUID tenantId);

    Page<Product> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Product> findByTenantIdAndStatus(UUID tenantId, ProductStatus status, Pageable pageable);

    Page<Product> findByTenantIdAndCategoryId(UUID tenantId, UUID categoryId, Pageable pageable);

    Page<Product> findByTenantIdAndCategoryIdAndStatus(UUID tenantId, UUID categoryId, ProductStatus status, Pageable pageable);

    List<Product> findByTenantIdAndStatusAndIsRentableTrue(UUID tenantId, ProductStatus status);

    List<Product> findByTenantIdAndStatusAndIsSellableTrue(UUID tenantId, ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchByTenantId(@Param("tenantId") UUID tenantId,
                                    @Param("search") String search,
                                    Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.status = :status " +
           "ORDER BY p.viewCount DESC")
    List<Product> findTopViewedByTenantId(@Param("tenantId") UUID tenantId,
                                           @Param("status") ProductStatus status,
                                           Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.status = :status " +
           "ORDER BY p.rentalCount DESC")
    List<Product> findTopRentedByTenantId(@Param("tenantId") UUID tenantId,
                                           @Param("status") ProductStatus status,
                                           Pageable pageable);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, ProductStatus status);

    long countByCategoryId(UUID categoryId);
}
