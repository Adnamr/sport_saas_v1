package com.sportsaas.catalog.infra.repository;

import com.sportsaas.catalog.domain.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ProductAttribute entity.
 */
@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, UUID> {

    List<ProductAttribute> findByProductIdOrderBySortOrder(UUID productId);

    List<ProductAttribute> findByProductIdAndIsVisibleTrue(UUID productId);

    @Query("SELECT DISTINCT pa.name FROM ProductAttribute pa WHERE pa.tenantId = :tenantId AND pa.isFilterable = true")
    List<String> findDistinctFilterableAttributeNames(@Param("tenantId") UUID tenantId);

    @Query("SELECT DISTINCT pa.value FROM ProductAttribute pa WHERE pa.tenantId = :tenantId AND pa.name = :name")
    List<String> findDistinctValuesByName(@Param("tenantId") UUID tenantId, @Param("name") String name);

    void deleteByProductId(UUID productId);
}
