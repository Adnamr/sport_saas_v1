# ⚙️ Prompt: Créer un service

> Pour les tickets de type "Service XXX"

---

## Prompt à copier:

```
Crée le service {SERVICE_NAME} pour le ticket {TICKET_ID}

## Spécifications
- Module: {MODULE}
- Entité principale: {ENTITY}

## Méthodes requises
{LISTE_DES_METHODES}

## Dépendances disponibles
- Repositories: {REPOS}
- Autres services: {SERVICES}

## Génère

1. **Interface Service** dans `{module}/domain/service/{Service}Service.java`
   - Méthodes avec JavaDoc

2. **Implémentation** dans `{module}/infra/service/{Service}ServiceImpl.java`
   - @Service, @RequiredArgsConstructor
   - @Transactional(readOnly = true) par défaut
   - @Transactional sur les mutations
   - Publier des events si création/modification

3. **Events** dans `{module}/domain/event/{Entity}CreatedEvent.java` (si applicable)
   - Record avec les données essentielles

4. **Tests unitaires** dans `{module}/src/test/.../service/{Service}ServiceImplTest.java`
   - Mocks avec Mockito
   - Tests des cas nominaux et d'erreur

Montre-moi le code complet pour chaque fichier.
```

---

## Exemple rempli:

```
Crée le service ProductService pour le ticket E4-006

## Spécifications
- Module: catalog
- Entité principale: Product

## Méthodes requises
- findAll(Pageable): Page<Product>
- findById(UUID): Optional<Product>
- findBySku(String): Optional<Product>
- findByCategory(UUID, Pageable): Page<Product>
- create(Product): Product
- update(UUID, UpdateProductRequest): Product
- delete(UUID): void (soft delete → status ARCHIVED)

## Dépendances disponibles
- Repositories: ProductRepository, CategoryRepository
- Autres services: aucun

## Génère

1. **Interface Service** dans `catalog/domain/service/ProductService.java`
2. **Implémentation** dans `catalog/infra/service/ProductServiceImpl.java`
3. **Events** dans `catalog/domain/event/ProductCreatedEvent.java`
4. **Tests unitaires** dans `catalog/src/test/java/.../ProductServiceImplTest.java`

Montre-moi le code complet pour chaque fichier.
```
