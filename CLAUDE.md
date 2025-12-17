# CLAUDE.md

> **Ce fichier est lu automatiquement par Claude Code.**
> Il définit le contexte et les règles du projet.

---

## 🎯 Projet

**Sport Equipment SaaS** - Plateforme multi-tenant de gestion de matériels sportifs.

## 📚 Avant de coder

**TOUJOURS lire ces fichiers en premier:**

1. `team/tickets/current.yaml` → Voir qui travaille sur quoi
2. `team/devs/{dev}.yaml` → Contexte du ticket en cours
3. `team/rules/code-standards.md` → Standards de code

## 🏗️ Architecture

```
backend/
├── app/           # Bootstrap (main)
├── common/        # Shared: exceptions, utils, events
├── config/        # Security, Tenant filters
├── auth/          # JWT Authentication
├── tenant/        # Multi-tenant management
├── catalog/       # Products & Categories
├── inventory/     # Stock & Reservations
├── order/         # Orders & Rentals
├── billing/       # Invoices & Payments
└── notification/  # Emails
```

### Structure d'un module

```
<module>/
├── api/           # Controllers, DTOs, Mappers
├── domain/        # Entities, Interfaces (PURE JAVA - NO DEPS)
└── infra/         # Implementations
```

## ⚠️ Règles CRITIQUES

### 1. Multi-tenant
```java
// TOUTES les entités héritent de:
public abstract class TenantAwareEntity {
    private UUID id;
    private UUID tenantId;  // OBLIGATOIRE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 2. Clean Architecture
- `domain/` → **AUCUNE** dépendance externe (pas de Spring, pas de JPA annotations sur les interfaces)
- `api/` et `infra/` → dépendent **UNIQUEMENT** de `domain/`
- **JAMAIS** d'import entre modules → utiliser `ApplicationEventPublisher`

### 3. Conventions de nommage

| Type | Pattern | Exemple |
|------|---------|---------|
| Entité | PascalCase | `Product` |
| Table | snake_case | `products` |
| Repository | `{Entity}Repository` | `ProductRepository` |
| Service | `{Entity}Service` | `ProductService` |
| Controller | `{Entity}Controller` | `ProductController` |
| DTO | `{Action}{Entity}Request` | `CreateProductRequest` |
| Migration | `V{n}__{desc}.sql` | `V3__create_products.sql` |

### 4. Format des commits
```
feat(E1-002): description - Refs #42
```

## 📁 Templates de code

### Entité
```java
@Entity
@Table(name = "products")
@Getter @Setter
public class Product extends TenantAwareEntity {
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.DRAFT;
}
```

### Service
```java
// Interface dans domain/service/
public interface ProductService {
    Page<Product> findAll(Pageable pageable);
    Product create(Product product);
}

// Implémentation dans infra/service/
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    // ...
}
```

### Controller
```java
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products")
public class ProductController {
    
    @GetMapping
    public Page<ProductResponse> list(Pageable pageable) { }
    
    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ProductResponse create(@Valid @RequestBody CreateProductRequest req) { }
}
```

### DTOs (records)
```java
public record CreateProductRequest(
    @NotBlank String name,
    @NotNull @Positive BigDecimal price
) {}
```

## 🔐 Sécurité

**Rôles:** `SUPER_ADMIN`, `TENANT_ADMIN`, `EMPLOYEE`, `CUSTOMER`

```java
@PreAuthorize("hasRole('TENANT_ADMIN')")      // Mutations
@PreAuthorize("isAuthenticated()")             // Lectures
```

## 📋 Workflow

```bash
# Voir les prompts prédéfinis
ls team/prompts/

# Prendre un ticket
./team/scripts/take-ticket.sh E1-002 alice 42

# Terminer un ticket
./team/scripts/complete-ticket.sh E1-002 alice
```

## 🔗 Liens

- **OpenProject:** https://aam.openproject.com/
- **Prompts:** `team/prompts/`
- **Standards:** `team/rules/code-standards.md`
