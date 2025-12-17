# 📏 Standards de Code

> Règles que Claude Code doit TOUJOURS respecter

---

## ☑️ Checklist avant de générer du code

- [ ] Vérifier le module concerné dans `team/devs/{dev}.yaml`
- [ ] Vérifier que les dépendances sont terminées
- [ ] Respecter la structure Clean Architecture
- [ ] Entité hérite de `TenantAwareEntity`
- [ ] Pas d'import entre modules

---

## 🏗️ Structure des fichiers

### Pour une Entité

```
{module}/
├── domain/
│   ├── model/
│   │   ├── {Entity}.java           # Entité JPA
│   │   └── {Entity}Status.java     # Enum si nécessaire
│   └── repository/
│       └── {Entity}Repository.java # Interface
└── src/main/resources/db/migration/
    └── V{n}__create_{table}.sql    # Migration Flyway
```

### Pour un Service

```
{module}/
├── domain/
│   ├── service/
│   │   └── {Entity}Service.java    # Interface
│   └── event/
│       └── {Entity}CreatedEvent.java
└── infra/
    └── service/
        └── {Entity}ServiceImpl.java
```

### Pour une API

```
{module}/
└── api/
    ├── controller/
    │   └── {Entity}Controller.java
    ├── dto/
    │   ├── Create{Entity}Request.java
    │   ├── Update{Entity}Request.java
    │   └── {Entity}Response.java
    └── mapper/
        └── {Entity}Mapper.java
```

---

## 📝 Conventions de code

### Entités JPA

```java
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_tenant", columnList = "tenant_id"),
    @Index(name = "idx_products_tenant_sku", columnList = "tenant_id, sku", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Product extends TenantAwareEntity {

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
```

### Repository

```java
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);
    
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);
    
    boolean existsBySku(String sku);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);
}
```

### Service Interface

```java
public interface ProductService {

    /**
     * Liste tous les produits avec pagination
     */
    Page<Product> findAll(Pageable pageable);

    /**
     * Recherche un produit par ID
     * @throws NotFoundException si non trouvé
     */
    Product findById(UUID id);

    /**
     * Crée un nouveau produit
     * @throws ValidationException si SKU déjà existant
     */
    Product create(Product product);

    /**
     * Met à jour un produit existant
     */
    Product update(UUID id, UpdateProductRequest request);

    /**
     * Supprime un produit (soft delete)
     */
    void delete(UUID id);
}
```

### Service Implementation

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product findById(UUID id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product", id));
    }

    @Override
    @Transactional
    public Product create(Product product) {
        // Validation
        if (productRepository.existsBySku(product.getSku())) {
            throw new ValidationException("SKU already exists: " + product.getSku());
        }

        // Save
        Product saved = productRepository.save(product);
        
        // Event
        eventPublisher.publishEvent(new ProductCreatedEvent(saved.getId(), saved.getTenantId()));
        
        log.info("Product created: {} ({})", saved.getName(), saved.getSku());
        return saved;
    }

    @Override
    @Transactional
    public Product update(UUID id, UpdateProductRequest request) {
        Product product = findById(id);
        
        // Update fields
        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        // ... autres champs
        
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = findById(id);
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
        
        log.info("Product archived: {}", id);
    }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Gestion des produits")
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    @Operation(summary = "Liste des produits", description = "Retourne la liste paginée des produits")
    public Page<ProductResponse> list(
            @ParameterObject Pageable pageable) {
        return productService.findAll(pageable)
            .map(productMapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un produit")
    public ProductResponse getById(
            @PathVariable UUID id) {
        return productMapper.toResponse(productService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Créer un produit")
    public ProductResponse create(
            @Valid @RequestBody CreateProductRequest request) {
        Product product = productMapper.toEntity(request);
        return productMapper.toResponse(productService.create(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Modifier un produit")
    public ProductResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return productMapper.toResponse(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Supprimer un produit")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}
```

### DTOs

```java
// Request - Création
public record CreateProductRequest(
    @NotBlank(message = "SKU is required")
    @Size(max = 100)
    String sku,
    
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    String name,
    
    String description,
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    BigDecimal price,
    
    @Positive
    BigDecimal rentalPricePerDay,
    
    UUID categoryId,
    
    @NotNull
    ProductType type
) {}

// Request - Mise à jour (tous les champs optionnels)
public record UpdateProductRequest(
    @Size(max = 255)
    String name,
    
    String description,
    
    @Positive
    BigDecimal price,
    
    @Positive
    BigDecimal rentalPricePerDay,
    
    UUID categoryId,
    
    ProductStatus status
) {}

// Response
public record ProductResponse(
    UUID id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    BigDecimal rentalPricePerDay,
    UUID categoryId,
    String categoryName,
    ProductType type,
    ProductStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### Mapper (MapStruct)

```java
@Mapper(componentModel = "spring", uses = {})
public interface ProductMapper {

    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(CreateProductRequest request);
}
```

### Migration Flyway

```sql
-- V5__create_products_table.sql

CREATE TABLE products (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    rental_price_per_day DECIMAL(10, 2),
    category_id UUID REFERENCES categories(id),
    type VARCHAR(20) NOT NULL DEFAULT 'SALE',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_products_price CHECK (price > 0),
    CONSTRAINT chk_products_rental_price CHECK (rental_price_per_day IS NULL OR rental_price_per_day > 0)
);

-- Index obligatoire sur tenant_id
CREATE INDEX idx_products_tenant ON products(tenant_id);

-- Index unique par tenant
CREATE UNIQUE INDEX idx_products_tenant_sku ON products(tenant_id, sku);

-- Index pour les recherches fréquentes
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);

-- Trigger pour updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();
```

---

## ❌ À NE JAMAIS FAIRE

```java
// ❌ Import entre modules
import com.sportsaas.order.domain.model.Order;  // INTERDIT dans catalog

// ❌ Dépendance dans domain/
import org.springframework.stereotype.Service;  // INTERDIT dans domain/

// ❌ Entité sans tenant_id
public class Product {  // DOIT hériter de TenantAwareEntity

// ❌ Query sans filtre tenant
SELECT * FROM products;  // TOUJOURS filtrer par tenant_id

// ❌ Hard delete
productRepository.delete(product);  // Utiliser soft delete (status = ARCHIVED)
```
