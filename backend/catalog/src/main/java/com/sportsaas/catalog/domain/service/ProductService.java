package com.sportsaas.catalog.domain.service;

import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Product operations.
 */
public interface ProductService {

    Product create(Product product);

    Product update(UUID id, Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    Page<Product> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Product> findByTenantIdAndStatus(UUID tenantId, ProductStatus status, Pageable pageable);

    Page<Product> findByCategory(UUID tenantId, UUID categoryId, Pageable pageable);

    Page<Product> search(UUID tenantId, String search, Pageable pageable);

    List<Product> findRentableProducts(UUID tenantId);

    List<Product> findSellableProducts(UUID tenantId);

    List<Product> findTopViewed(UUID tenantId, int limit);

    List<Product> findTopRented(UUID tenantId, int limit);

    boolean isSlugAvailable(String slug, UUID tenantId);

    boolean isSkuAvailable(String sku, UUID tenantId);

    void delete(UUID id);

    void activate(UUID id);

    void deactivate(UUID id);

    void incrementViewCount(UUID id);

    void incrementRentalCount(UUID id);

    long countByTenantId(UUID tenantId);

    long countByCategory(UUID categoryId);
}
