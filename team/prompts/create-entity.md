# 🗄️ Prompt: Créer une entité

> Pour les tickets de type "Modèle XXX"

---

## Prompt à copier:

```
Crée l'entité {ENTITY_NAME} pour le ticket {TICKET_ID}

## Spécifications
- Module: {MODULE}
- Table SQL: {TABLE_NAME}

## Champs requis
{LISTE_DES_CHAMPS}

## Contraintes
- Hérite de TenantAwareEntity
- Index unique sur (tenant_id, {CHAMP_UNIQUE})
- Enums si nécessaire: {ENUMS}

## Génère

1. **Entité JPA** dans `{module}/domain/model/{Entity}.java`
   - Annotations JPA complètes
   - Lombok (@Getter, @Setter)
   - Relations si nécessaire

2. **Enums** dans `{module}/domain/model/{Enum}.java`

3. **Repository** dans `{module}/domain/repository/{Entity}Repository.java`
   - Interface extends JpaRepository
   - Méthodes de recherche utiles

4. **Migration Flyway** dans `backend/app/src/main/resources/db/migration/V{N}__create_{table}_table.sql`
   - CREATE TABLE avec tous les champs
   - Index sur tenant_id
   - Index unique si spécifié
   - Foreign keys si nécessaire

Montre-moi le code complet pour chaque fichier.
```

---

## Exemple rempli:

```
Crée l'entité Product pour le ticket E4-002

## Spécifications
- Module: catalog
- Table SQL: products

## Champs requis
- sku: String (unique par tenant)
- name: String (obligatoire)
- description: String (optionnel)
- price: BigDecimal (obligatoire, positif)
- rentalPricePerDay: BigDecimal (optionnel)
- categoryId: UUID (FK vers categories)
- type: ProductType enum
- status: ProductStatus enum

## Contraintes
- Hérite de TenantAwareEntity
- Index unique sur (tenant_id, sku)
- Enums: ProductType (SALE, RENTAL, BOTH), ProductStatus (DRAFT, ACTIVE, ARCHIVED)

## Génère

1. **Entité JPA** dans `catalog/domain/model/Product.java`
2. **Enums** dans `catalog/domain/model/ProductType.java` et `ProductStatus.java`
3. **Repository** dans `catalog/domain/repository/ProductRepository.java`
4. **Migration Flyway** dans `backend/app/src/main/resources/db/migration/V5__create_products_table.sql`

Montre-moi le code complet pour chaque fichier.
```
