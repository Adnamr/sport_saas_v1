package com.sportsaas.catalog.infra.service;

import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.enums.ProductStatus;
import com.sportsaas.catalog.domain.service.ProductService;
import com.sportsaas.catalog.infra.repository.ProductRepository;
import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of ProductService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Product create(Product product) {
        if (productRepository.existsBySlugAndTenantId(product.getSlug(), product.getTenantId())) {
            throw new ConflictException("Product with slug " + product.getSlug() + " already exists");
        }

        if (productRepository.existsBySkuAndTenantId(product.getSku(), product.getTenantId())) {
            throw new ConflictException("Product with SKU " + product.getSku() + " already exists");
        }

        log.info("Creating product: {}", product.getName());
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(UUID id, Product product) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        // Check slug uniqueness if changed
        if (!existing.getSlug().equals(product.getSlug()) &&
            productRepository.existsBySlugAndTenantId(product.getSlug(), existing.getTenantId())) {
            throw new ConflictException("Product with slug " + product.getSlug() + " already exists");
        }

        // Check SKU uniqueness if changed
        if (!existing.getSku().equals(product.getSku()) &&
            productRepository.existsBySkuAndTenantId(product.getSku(), existing.getTenantId())) {
            throw new ConflictException("Product with SKU " + product.getSku() + " already exists");
        }

        existing.setName(product.getName());
        existing.setSlug(product.getSlug());
        existing.setSku(product.getSku());
        existing.setDescription(product.getDescription());
        existing.setShortDescription(product.getShortDescription());
        existing.setCategory(product.getCategory());
        existing.setCondition(product.getCondition());
        existing.setPurchasePrice(product.getPurchasePrice());
        existing.setRentalPriceDaily(product.getRentalPriceDaily());
        existing.setRentalPriceWeekly(product.getRentalPriceWeekly());
        existing.setRentalPriceMonthly(product.getRentalPriceMonthly());
        existing.setSalePrice(product.getSalePrice());
        existing.setDepositAmount(product.getDepositAmount());
        existing.setBrand(product.getBrand());
        existing.setModel(product.getModel());
        existing.setSerialNumber(product.getSerialNumber());
        existing.setSize(product.getSize());
        existing.setColor(product.getColor());
        existing.setWeight(product.getWeight());
        existing.setDimensions(product.getDimensions());
        existing.setIsRentable(product.getIsRentable());
        existing.setIsSellable(product.getIsSellable());
        existing.setRequiresDeposit(product.getRequiresDeposit());
        existing.setMinRentalDays(product.getMinRentalDays());
        existing.setMaxRentalDays(product.getMaxRentalDays());

        return productRepository.save(existing);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Override
    public Page<Product> findByTenantId(UUID tenantId, Pageable pageable) {
        return productRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public Page<Product> findByTenantIdAndStatus(UUID tenantId, ProductStatus status, Pageable pageable) {
        return productRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    @Override
    public Page<Product> findByCategory(UUID tenantId, UUID categoryId, Pageable pageable) {
        return productRepository.findByTenantIdAndCategoryId(tenantId, categoryId, pageable);
    }

    @Override
    public Page<Product> search(UUID tenantId, String search, Pageable pageable) {
        return productRepository.searchByTenantId(tenantId, search, pageable);
    }

    @Override
    public List<Product> findRentableProducts(UUID tenantId) {
        return productRepository.findByTenantIdAndStatusAndIsRentableTrue(tenantId, ProductStatus.ACTIVE);
    }

    @Override
    public List<Product> findSellableProducts(UUID tenantId) {
        return productRepository.findByTenantIdAndStatusAndIsSellableTrue(tenantId, ProductStatus.ACTIVE);
    }

    @Override
    public List<Product> findTopViewed(UUID tenantId, int limit) {
        return productRepository.findTopViewedByTenantId(tenantId, ProductStatus.ACTIVE, PageRequest.of(0, limit));
    }

    @Override
    public List<Product> findTopRented(UUID tenantId, int limit) {
        return productRepository.findTopRentedByTenantId(tenantId, ProductStatus.ACTIVE, PageRequest.of(0, limit));
    }

    @Override
    public boolean isSlugAvailable(String slug, UUID tenantId) {
        return !productRepository.existsBySlugAndTenantId(slug, tenantId);
    }

    @Override
    public boolean isSkuAvailable(String sku, UUID tenantId) {
        return !productRepository.existsBySkuAndTenantId(sku, tenantId);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void incrementViewCount(UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void incrementRentalCount(UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        product.setRentalCount(product.getRentalCount() + 1);
        productRepository.save(product);
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return productRepository.countByTenantId(tenantId);
    }

    @Override
    public long countByCategory(UUID categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
}
