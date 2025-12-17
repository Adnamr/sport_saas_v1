package com.sportsaas.catalog.domain;

import com.sportsaas.catalog.domain.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Category entity.
 */
class CategoryTest {

    private Category rootCategory;
    private Category childCategory;
    private Category grandChildCategory;

    @BeforeEach
    void setUp() {
        rootCategory = Category.builder()
            .name("Sports")
            .slug("sports")
            .description("All sports equipment")
            .sortOrder(0)
            .active(true)
            .productCount(0)
            .tenantId(UUID.randomUUID())
            .children(new ArrayList<>())
            .build();

        childCategory = Category.builder()
            .name("Team Sports")
            .slug("team-sports")
            .description("Team sports equipment")
            .sortOrder(0)
            .active(true)
            .productCount(5)
            .tenantId(UUID.randomUUID())
            .parent(rootCategory)
            .children(new ArrayList<>())
            .build();

        grandChildCategory = Category.builder()
            .name("Football")
            .slug("football")
            .description("Football equipment")
            .sortOrder(0)
            .active(true)
            .productCount(10)
            .tenantId(UUID.randomUUID())
            .parent(childCategory)
            .children(new ArrayList<>())
            .build();
    }

    @Test
    void shouldBeRootWhenNoParent() {
        // When/Then
        assertThat(rootCategory.isRoot()).isTrue();
    }

    @Test
    void shouldNotBeRootWhenHasParent() {
        // When/Then
        assertThat(childCategory.isRoot()).isFalse();
    }

    @Test
    void shouldReturnZeroLevelForRootCategory() {
        // When
        int level = rootCategory.getLevel();

        // Then
        assertThat(level).isZero();
    }

    @Test
    void shouldReturnOneLevelForChildCategory() {
        // When
        int level = childCategory.getLevel();

        // Then
        assertThat(level).isEqualTo(1);
    }

    @Test
    void shouldReturnTwoLevelForGrandChildCategory() {
        // When
        int level = grandChildCategory.getLevel();

        // Then
        assertThat(level).isEqualTo(2);
    }

    @Test
    void shouldReturnNameAsFullPathForRootCategory() {
        // When
        String fullPath = rootCategory.getFullPath();

        // Then
        assertThat(fullPath).isEqualTo("Sports");
    }

    @Test
    void shouldReturnFullPathWithParentForChildCategory() {
        // When
        String fullPath = childCategory.getFullPath();

        // Then
        assertThat(fullPath).isEqualTo("Sports > Team Sports");
    }

    @Test
    void shouldReturnFullPathWithAllAncestorsForGrandChildCategory() {
        // When
        String fullPath = grandChildCategory.getFullPath();

        // Then
        assertThat(fullPath).isEqualTo("Sports > Team Sports > Football");
    }

    @Test
    void shouldHaveDefaultActiveTrue() {
        // Given
        Category newCategory = Category.builder()
            .name("New Category")
            .slug("new-category")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newCategory.getActive()).isTrue();
    }

    @Test
    void shouldHaveDefaultSortOrderZero() {
        // Given
        Category newCategory = Category.builder()
            .name("New Category")
            .slug("new-category")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newCategory.getSortOrder()).isZero();
    }

    @Test
    void shouldHaveDefaultProductCountZero() {
        // Given
        Category newCategory = Category.builder()
            .name("New Category")
            .slug("new-category")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newCategory.getProductCount()).isZero();
    }

    @Test
    void shouldHaveEmptyChildrenListByDefault() {
        // Given
        Category newCategory = Category.builder()
            .name("New Category")
            .slug("new-category")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newCategory.getChildren()).isNotNull().isEmpty();
    }
}
