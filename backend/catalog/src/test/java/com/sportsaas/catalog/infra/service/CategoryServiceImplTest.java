package com.sportsaas.catalog.infra.service;

import com.sportsaas.catalog.domain.entity.Category;
import com.sportsaas.catalog.infra.repository.CategoryRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CategoryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private UUID categoryId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        testCategory = Category.builder()
            .name("Sports Equipment")
            .slug("sports-equipment")
            .description("All sports equipment")
            .sortOrder(0)
            .active(true)
            .productCount(0)
            .tenantId(tenantId)
            .children(new ArrayList<>())
            .build();
    }

    @Test
    void shouldCreateCategory() {
        // Given
        when(categoryRepository.existsBySlugAndTenantId("sports-equipment", tenantId)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        Category result = categoryService.create(testCategory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sports Equipment");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenSlugExists() {
        // Given
        when(categoryRepository.existsBySlugAndTenantId("sports-equipment", tenantId)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.create(testCategory))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldUpdateCategory() {
        // Given
        Category existing = Category.builder()
            .name("Old Name")
            .slug("old-slug")
            .description("Old description")
            .sortOrder(0)
            .active(true)
            .productCount(0)
            .tenantId(tenantId)
            .children(new ArrayList<>())
            .build();

        Category updated = Category.builder()
            .name("New Name")
            .slug("new-slug")
            .description("New description")
            .sortOrder(1)
            .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsBySlugAndTenantId("new-slug", tenantId)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(existing);

        // When
        Category result = categoryService.update(categoryId, updated);

        // Then
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getSlug()).isEqualTo("new-slug");
        verify(categoryRepository).save(existing);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentCategory() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.update(categoryId, testCategory))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    void shouldFindCategoryById() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // When
        Optional<Category> result = categoryService.findById(categoryId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Sports Equipment");
    }

    @Test
    void shouldFindCategoryBySlug() {
        // Given
        when(categoryRepository.findBySlug("sports-equipment")).thenReturn(Optional.of(testCategory));

        // When
        Optional<Category> result = categoryService.findBySlug("sports-equipment");

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void shouldFindCategoriesByTenantId() {
        // Given
        Page<Category> page = new PageImpl<>(List.of(testCategory));
        when(categoryRepository.findByTenantId(tenantId, PageRequest.of(0, 10))).thenReturn(page);

        // When
        Page<Category> result = categoryService.findByTenantId(tenantId, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindRootCategories() {
        // Given
        when(categoryRepository.findByTenantIdAndParentIsNullAndActiveTrue(tenantId))
            .thenReturn(List.of(testCategory));

        // When
        List<Category> result = categoryService.findRootCategories(tenantId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sports Equipment");
    }

    @Test
    void shouldFindActiveCategories() {
        // Given
        when(categoryRepository.findAllActiveByTenantIdOrdered(tenantId))
            .thenReturn(List.of(testCategory));

        // When
        List<Category> result = categoryService.findActiveCategories(tenantId);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFindCategoriesByParentId() {
        // Given
        UUID parentId = UUID.randomUUID();
        Category child = Category.builder()
            .name("Child Category")
            .slug("child-category")
            .active(true)
            .tenantId(tenantId)
            .children(new ArrayList<>())
            .build();

        when(categoryRepository.findByParentIdAndActiveTrue(parentId)).thenReturn(List.of(child));

        // When
        List<Category> result = categoryService.findByParentId(parentId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Child Category");
    }

    @Test
    void shouldSearchCategories() {
        // Given
        Page<Category> page = new PageImpl<>(List.of(testCategory));
        when(categoryRepository.searchByTenantId(eq(tenantId), eq("sports"), any())).thenReturn(page);

        // When
        Page<Category> result = categoryService.search(tenantId, "sports", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldCheckSlugAvailability() {
        // Given
        when(categoryRepository.existsBySlugAndTenantId("new-slug", tenantId)).thenReturn(false);

        // When
        boolean result = categoryService.isSlugAvailable("new-slug", tenantId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDeleteCategory() {
        // Given
        testCategory.setChildren(new ArrayList<>());
        testCategory.setProductCount(0);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // When
        categoryService.delete(categoryId);

        // Then
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void shouldThrowConflictWhenDeletingCategoryWithChildren() {
        // Given
        Category child = Category.builder()
            .name("Child")
            .slug("child")
            .tenantId(tenantId)
            .children(new ArrayList<>())
            .build();
        testCategory.setChildren(List.of(child));

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // When/Then
        assertThatThrownBy(() -> categoryService.delete(categoryId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("subcategories");
    }

    @Test
    void shouldThrowConflictWhenDeletingCategoryWithProducts() {
        // Given
        testCategory.setChildren(new ArrayList<>());
        testCategory.setProductCount(5);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // When/Then
        assertThatThrownBy(() -> categoryService.delete(categoryId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("products");
    }

    @Test
    void shouldActivateCategory() {
        // Given
        testCategory.setActive(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        categoryService.activate(categoryId);

        // Then
        assertThat(testCategory.getActive()).isTrue();
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void shouldDeactivateCategory() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        categoryService.deactivate(categoryId);

        // Then
        assertThat(testCategory.getActive()).isFalse();
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void shouldCountByTenantId() {
        // Given
        when(categoryRepository.countByTenantId(tenantId)).thenReturn(10L);

        // When
        long result = categoryService.countByTenantId(tenantId);

        // Then
        assertThat(result).isEqualTo(10L);
    }
}
