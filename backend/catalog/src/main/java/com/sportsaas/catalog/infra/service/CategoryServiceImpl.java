package com.sportsaas.catalog.infra.service;

import com.sportsaas.catalog.domain.entity.Category;
import com.sportsaas.catalog.domain.service.CategoryService;
import com.sportsaas.catalog.infra.repository.CategoryRepository;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CategoryService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category create(Category category) {
        if (categoryRepository.existsBySlugAndTenantId(category.getSlug(), category.getTenantId())) {
            throw new ConflictException("Category with slug " + category.getSlug() + " already exists");
        }

        log.info("Creating category: {}", category.getName());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(UUID id, Category category) {
        Category existing = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        // Check slug uniqueness if changed
        if (!existing.getSlug().equals(category.getSlug()) &&
            categoryRepository.existsBySlugAndTenantId(category.getSlug(), existing.getTenantId())) {
            throw new ConflictException("Category with slug " + category.getSlug() + " already exists");
        }

        existing.setName(category.getName());
        existing.setSlug(category.getSlug());
        existing.setDescription(category.getDescription());
        existing.setImageUrl(category.getImageUrl());
        existing.setSortOrder(category.getSortOrder());

        if (category.getParent() != null) {
            existing.setParent(category.getParent());
        }

        return categoryRepository.save(existing);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Override
    public Page<Category> findByTenantId(UUID tenantId, Pageable pageable) {
        return categoryRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public List<Category> findRootCategories(UUID tenantId) {
        return categoryRepository.findByTenantIdAndParentIsNullAndActiveTrue(tenantId);
    }

    @Override
    public List<Category> findActiveCategories(UUID tenantId) {
        return categoryRepository.findAllActiveByTenantIdOrdered(tenantId);
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return categoryRepository.findByParentIdAndActiveTrue(parentId);
    }

    @Override
    public Page<Category> search(UUID tenantId, String search, Pageable pageable) {
        return categoryRepository.searchByTenantId(tenantId, search, pageable);
    }

    @Override
    public boolean isSlugAvailable(String slug, UUID tenantId) {
        return !categoryRepository.existsBySlugAndTenantId(slug, tenantId);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        // Check if category has children
        if (!category.getChildren().isEmpty()) {
            throw new ConflictException("Cannot delete category with subcategories");
        }

        // Check if category has products
        if (category.getProductCount() > 0) {
            throw new ConflictException("Cannot delete category with products");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        category.setActive(true);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return categoryRepository.countByTenantId(tenantId);
    }
}
