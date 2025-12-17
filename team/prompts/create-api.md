# 🌐 Prompt: Créer une API REST

> Pour les tickets de type "API REST XXX"

---

## Prompt à copier:

```
Crée l'API REST pour {ENTITY} - ticket {TICKET_ID}

## Spécifications
- Module: {MODULE}
- Base URL: /api/{resource}
- Service disponible: {SERVICE}

## Endpoints requis
{LISTE_ENDPOINTS}

## Sécurité
- GET: {ROLES_LECTURE}
- POST/PUT/DELETE: {ROLES_ECRITURE}

## Génère

1. **DTOs Request** dans `{module}/api/dto/`
   - Create{Entity}Request.java (record avec validation)
   - Update{Entity}Request.java

2. **DTO Response** dans `{module}/api/dto/{Entity}Response.java`
   - Record avec tous les champs à exposer

3. **Mapper** dans `{module}/api/mapper/{Entity}Mapper.java`
   - Interface MapStruct
   - toResponse(Entity)
   - toEntity(CreateRequest)

4. **Controller** dans `{module}/api/controller/{Entity}Controller.java`
   - @RestController, @RequestMapping
   - @Tag OpenAPI
   - @Operation sur chaque endpoint
   - @PreAuthorize pour la sécurité
   - Pagination sur les listes

5. **Tests d'intégration** dans `{module}/src/test/.../controller/{Entity}ControllerTest.java`

Montre-moi le code complet pour chaque fichier.
```

---

## Exemple rempli:

```
Crée l'API REST pour Product - ticket E4-008

## Spécifications
- Module: catalog
- Base URL: /api/products
- Service disponible: ProductService

## Endpoints requis
- GET /api/products (liste paginée avec filtres)
- GET /api/products/{id}
- POST /api/products
- PUT /api/products/{id}
- DELETE /api/products/{id}
- POST /api/products/{id}/images

## Sécurité
- GET: Tous les utilisateurs authentifiés
- POST/PUT/DELETE: TENANT_ADMIN uniquement

## Génère

1. **DTOs Request** dans `catalog/api/dto/`
   - CreateProductRequest.java
   - UpdateProductRequest.java

2. **DTO Response** dans `catalog/api/dto/ProductResponse.java`

3. **Mapper** dans `catalog/api/mapper/ProductMapper.java`

4. **Controller** dans `catalog/api/controller/ProductController.java`

5. **Tests d'intégration** dans `catalog/src/test/.../ProductControllerTest.java`

Montre-moi le code complet pour chaque fichier.
```
