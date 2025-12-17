# 🚀 Implémentation Feature Complète

## Contexte Projet

- **Projet**: Sport Equipment SaaS
- **Stack**: Spring Boot 3.3 + Angular 17 + PostgreSQL
- **Architecture**: Multi-tenant, Microservices modulaires

## Structure Backend

```
backend/
├── common/          # Shared utilities, DTOs, exceptions
├── config/          # Configuration centralisée
├── auth/            # Authentification & autorisation
├── tenant/          # Gestion multi-tenant
├── catalog/         # Catalogue produits
├── inventory/       # Gestion des stocks
├── order/           # Commandes
├── billing/         # Facturation
├── notification/    # Notifications
└── admin/           # Administration
```

## Instructions

Tu dois implémenter la feature **[FEATURE_NAME]** de bout en bout.

### Backend (dans l'ordre)

1. **Entity** dans `backend/{module}/src/main/java/.../domain/`
   - JPA annotations (@Entity, @Table, etc.)
   - Lombok (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
   - Validation constraints
   - Relations bidirectionnelles si nécessaire

2. **Repository** dans `backend/{module}/src/main/java/.../repository/`
   - Extends JpaRepository
   - Méthodes de recherche custom avec @Query si nécessaire
   - JpaSpecificationExecutor pour les filtres complexes

3. **DTOs** dans `backend/{module}/src/main/java/.../api/dto/`
   - Records Java (immutables)
   - Validation avec @Valid
   - CreateXxxRequest, UpdateXxxRequest, XxxResponse

4. **Service** dans `backend/{module}/src/main/java/.../service/`
   - Interface + Impl
   - Transactions (@Transactional)
   - Exceptions métier
   - Logging

5. **Controller** dans `backend/{module}/src/main/java/.../api/`
   - REST endpoints
   - @Valid pour validation
   - Swagger annotations (@Operation, @ApiResponse)
   - Sécurité (@PreAuthorize)

6. **Tests** dans `backend/{module}/src/test/java/.../`
   - Tests unitaires Service avec Mockito
   - Tests d'intégration Controller avec @SpringBootTest

### Frontend (dans l'ordre)

1. **Models** dans `frontend/src/app/features/{module}/models/`
   - Interfaces TypeScript
   - Enums si nécessaire

2. **Service** dans `frontend/src/app/features/{module}/services/`
   - HttpClient calls
   - Error handling
   - Caching si approprié

3. **Components** dans `frontend/src/app/features/{module}/components/`
   - Standalone components
   - Reactive forms avec validation
   - Angular Material

4. **Pages** dans `frontend/src/app/features/{module}/pages/`
   - List page avec DataTable, filtres, pagination
   - Detail page
   - Form page (create/edit)

5. **Routing** dans `frontend/src/app/features/{module}/`
   - Routes avec guards si nécessaire
   - Lazy loading

## Conventions de Code

### Backend (Java)
```java
// Entity
@Entity
@Table(name = "entities")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Entity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    @NotBlank
    private String name;
}

// DTO (Record)
public record CreateEntityRequest(
    @NotBlank String name,
    @Email String email
) {}

// Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntityServiceImpl implements EntityService {
    private final EntityRepository repository;
    
    @Override
    @Transactional
    public Entity create(CreateEntityRequest request) {
        // ...
    }
}

// Controller
@RestController
@RequestMapping("/api/entities")
@RequiredArgsConstructor
@Tag(name = "Entities")
public class EntityController {
    
    @GetMapping
    @Operation(summary = "List entities")
    public Page<EntityResponse> list(Pageable pageable) {
        // ...
    }
}
```

### Frontend (TypeScript/Angular)
```typescript
// Model
export interface Entity {
  id: string;
  name: string;
}

// Service
@Injectable({ providedIn: 'root' })
export class EntityService {
  private readonly apiUrl = '/api/entities';
  
  constructor(private http: HttpClient) {}
  
  list(params?: any): Observable<Page<Entity>> {
    return this.http.get<Page<Entity>>(this.apiUrl, { params });
  }
}

// Component
@Component({
  selector: 'app-entity-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, ...],
  template: `...`
})
export class EntityListComponent {
  // ...
}
```

## Format de Commit

```
feat({module}): {description} - Refs #{id}
```

Exemple: `feat(tenant): implement club management CRUD - Refs #123`

## Checklist

- [ ] Entity avec validations
- [ ] Repository avec méthodes custom
- [ ] DTOs (Request/Response)
- [ ] Service avec tests unitaires
- [ ] Controller avec tests d'intégration
- [ ] Frontend service
- [ ] Frontend components/pages
- [ ] Documentation Swagger

---

**Génère maintenant tous les fichiers pour implémenter cette feature.**
