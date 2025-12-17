package com.sportsaas.catalog.infra.service;

import com.sportsaas.catalog.domain.entity.Category;
import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.enums.ProductCondition;
import com.sportsaas.catalog.domain.enums.ProductStatus;
import com.sportsaas.catalog.infra.repository.ProductRepository;
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

import java.math.BigDecimal;
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
 * Unit tests for ProductServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;
    private UUID productId;
    private UUID categoryId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        testCategory = Category.builder()
            .name("Sports Equipment")
            .slug("sports-equipment")
            .tenantId(tenantId)
            .build();

        testProduct = Product.builder()
            .name("Mountain Bike")
            .slug("mountain-bike")
            .sku("MTB-001")
            .description("Professional mountain bike")
            .shortDescription("Pro MTB")
            .category(testCategory)
            .status(ProductStatus.DRAFT)
            .condition(ProductCondition.NEW)
            .rentalPriceDaily(new BigDecimal("50.00"))
            .rentalPriceWeekly(new BigDecimal("280.00"))
            .rentalPriceMonthly(new BigDecimal("900.00"))
            .depositAmount(new BigDecimal("500.00"))
            .brand("Trek")
            .model("X-Caliber 8")
            .isRentable(true)
            .isSellable(false)
            .requiresDeposit(true)
            .minRentalDays(1)
            .viewCount(0)
            .rentalCount(0)
            .tenantId(tenantId)
            .build();
    }

    @Test
    void shouldCreateProduct() {
        // Given
        when(productRepository.existsBySlugAndTenantId("mountain-bike", tenantId)).thenReturn(false);
        when(productRepository.existsBySkuAndTenantId("MTB-001", tenantId)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.create(testProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Mountain Bike");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenSlugExists() {
        // Given
        when(productRepository.existsBySlugAndTenantId("mountain-bike", tenantId)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> productService.create(testProduct))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("slug")
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldThrowExceptionWhenSkuExists() {
        // Given
        when(productRepository.existsBySlugAndTenantId("mountain-bike", tenantId)).thenReturn(false);
        when(productRepository.existsBySkuAndTenantId("MTB-001", tenantId)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> productService.create(testProduct))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("SKU")
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldUpdateProduct() {
        // Given
        Product existing = Product.builder()
            .name("Old Bike")
            .slug("old-bike")
            .sku("OLD-001")
            .tenantId(tenantId)
            .build();

        Product updated = Product.builder()
            .name("New Bike")
            .slug("new-bike")
            .sku("NEW-001")
            .description("Updated description")
            .category(testCategory)
            .rentalPriceDaily(new BigDecimal("60.00"))
            .isRentable(true)
            .isSellable(true)
            .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySlugAndTenantId("new-bike", tenantId)).thenReturn(false);
        when(productRepository.existsBySkuAndTenantId("NEW-001", tenantId)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        // When
        Product result = productService.update(productId, updated);

        // Then
        assertThat(result.getName()).isEqualTo("New Bike");
        assertThat(result.getSlug()).isEqualTo("new-bike");
        verify(productRepository).save(existing);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentProduct() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.update(productId, testProduct))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void shouldFindProductById() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.findById(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Mountain Bike");
    }

    @Test
    void shouldFindProductBySlug() {
        // Given
        when(productRepository.findBySlug("mountain-bike")).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.findBySlug("mountain-bike");

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void shouldFindProductBySku() {
        // Given
        when(productRepository.findBySku("MTB-001")).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.findBySku("MTB-001");

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void shouldFindProductsByTenantId() {
        // Given
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.findByTenantId(tenantId, PageRequest.of(0, 10))).thenReturn(page);

        // When
        Page<Product> result = productService.findByTenantId(tenantId, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindProductsByTenantIdAndStatus() {
        // Given
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.findByTenantIdAndStatus(tenantId, ProductStatus.ACTIVE, PageRequest.of(0, 10)))
            .thenReturn(page);

        // When
        Page<Product> result = productService.findByTenantIdAndStatus(tenantId, ProductStatus.ACTIVE, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindProductsByCategory() {
        // Given
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.findByTenantIdAndCategoryId(tenantId, categoryId, PageRequest.of(0, 10)))
            .thenReturn(page);

        // When
        Page<Product> result = productService.findByCategory(tenantId, categoryId, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldSearchProducts() {
        // Given
        Page<Product> page = new PageImpl<>(List.of(testProduct));
        when(productRepository.searchByTenantId(eq(tenantId), eq("bike"), any())).thenReturn(page);

        // When
        Page<Product> result = productService.search(tenantId, "bike", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindRentableProducts() {
        // Given
        when(productRepository.findByTenantIdAndStatusAndIsRentableTrue(tenantId, ProductStatus.ACTIVE))
            .thenReturn(List.of(testProduct));

        // When
        List<Product> result = productService.findRentableProducts(tenantId);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFindSellableProducts() {
        // Given
        when(productRepository.findByTenantIdAndStatusAndIsSellableTrue(tenantId, ProductStatus.ACTIVE))
            .thenReturn(List.of(testProduct));

        // When
        List<Product> result = productService.findSellableProducts(tenantId);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFindTopViewedProducts() {
        // Given
        when(productRepository.findTopViewedByTenantId(eq(tenantId), eq(ProductStatus.ACTIVE), any()))
            .thenReturn(List.of(testProduct));

        // When
        List<Product> result = productService.findTopViewed(tenantId, 10);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFindTopRentedProducts() {
        // Given
        when(productRepository.findTopRentedByTenantId(eq(tenantId), eq(ProductStatus.ACTIVE), any()))
            .thenReturn(List.of(testProduct));

        // When
        List<Product> result = productService.findTopRented(tenantId, 10);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldCheckSlugAvailability() {
        // Given
        when(productRepository.existsBySlugAndTenantId("new-slug", tenantId)).thenReturn(false);

        // When
        boolean result = productService.isSlugAvailable("new-slug", tenantId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldCheckSkuAvailability() {
        // Given
        when(productRepository.existsBySkuAndTenantId("NEW-SKU", tenantId)).thenReturn(false);

        // When
        boolean result = productService.isSkuAvailable("NEW-SKU", tenantId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldDeleteProduct() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        productService.delete(productId);

        // Then
        verify(productRepository).delete(testProduct);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentProduct() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.delete(productId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void shouldActivateProduct() {
        // Given
        testProduct.setStatus(ProductStatus.DRAFT);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.activate(productId);

        // Then
        assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        verify(productRepository).save(testProduct);
    }

    @Test
    void shouldDeactivateProduct() {
        // Given
        testProduct.setStatus(ProductStatus.ACTIVE);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.deactivate(productId);

        // Then
        assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        verify(productRepository).save(testProduct);
    }

    @Test
    void shouldIncrementViewCount() {
        // Given
        testProduct.setViewCount(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.incrementViewCount(productId);

        // Then
        assertThat(testProduct.getViewCount()).isEqualTo(11);
        verify(productRepository).save(testProduct);
    }

    @Test
    void shouldIncrementRentalCount() {
        // Given
        testProduct.setRentalCount(5);
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.incrementRentalCount(productId);

        // Then
        assertThat(testProduct.getRentalCount()).isEqualTo(6);
        verify(productRepository).save(testProduct);
    }

    @Test
    void shouldCountByTenantId() {
        // Given
        when(productRepository.countByTenantId(tenantId)).thenReturn(25L);

        // When
        long result = productService.countByTenantId(tenantId);

        // Then
        assertThat(result).isEqualTo(25L);
    }

    @Test
    void shouldCountByCategory() {
        // Given
        when(productRepository.countByCategoryId(categoryId)).thenReturn(15L);

        // When
        long result = productService.countByCategory(categoryId);

        // Then
        assertThat(result).isEqualTo(15L);
    }
}
