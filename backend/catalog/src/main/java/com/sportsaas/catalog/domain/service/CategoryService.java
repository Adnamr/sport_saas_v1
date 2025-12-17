package com.sportsaas.catalog.domain.service;

import com.sportsaas.catalog.domain.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Category operations.
 */
public interface CategoryService {

    Category create(Category category);

    Category update(UUID id, Category category);

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    Page<Category> findByTenantId(UUID tenantId, Pageable pageable);

    List<Category> findRootCategories(UUID tenantId);

    List<Category> findActiveCategories(UUID tenantId);

    List<Category> findByParentId(UUID parentId);

    Page<Category> search(UUID tenantId, String search, Pageable pageable);

    boolean isSlugAvailable(String slug, UUID tenantId);

    void delete(UUID id);

    void activate(UUID id);

    void deactivate(UUID id);

    long countByTenantId(UUID tenantId);
}
