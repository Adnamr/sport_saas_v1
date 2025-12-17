# 🚀 GETTING STARTED - Sport Equipment SaaS

## Vue d'ensemble

Ce projet utilise un **workflow feature-based** optimisé pour Claude Code.
Au lieu de faire 10+ PRs par fonctionnalité, on implémente tout d'un coup.

```
┌─────────────────────────────────────────────────────────────┐
│  WORKFLOW TRADITIONNEL          vs    WORKFLOW FEATURES    │
├─────────────────────────────────────────────────────────────┤
│  10+ tickets                          1 feature            │
│  10+ PRs                              1 PR                 │
│  4+ heures                            1-2 heures           │
│  Contexte perdu                       Contexte préservé    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Étapes d'initialisation (OBLIGATOIRES)

### Étape 1: Cloner et initialiser

```bash
git clone <repo-url>
cd sport-saas
make init
```

### Étape 2: Configurer les accès

```bash
make access
```

Cela configure:
- ✅ Token GitHub
- ✅ Token OpenProject
- ✅ Fichier `~/.sport-saas/config`

### Étape 3: Créer ton profil développeur

```bash
make dev DEV=ton-prenom
```

Cela crée `team/devs/ton-prenom.yaml` avec tes infos.

### Étape 4: Démarrer les services

```bash
make up          # Docker (PostgreSQL, etc.)
make backend     # Spring Boot (terminal 1)
make frontend    # Angular (terminal 2)
```

---

## 🎯 Workflow Features (Recommandé)

### 1. Voir les features disponibles

```bash
make features
```

```
═══════════════════════════════════════════════════════════════
              📋 FEATURES DISPONIBLES
═══════════════════════════════════════════════════════════════

  ○ e1-module-configuration  [high] ~2j  Setup Spring Boot, PostgreSQL
  ○ e2-module-tenant         [high] ~3j  Multi-tenant, BaseEntity
  ○ e3-module-endpoint       [high] ~5j  Auth, JWT, RBAC
  ○ e4-module-category       [high] ~4j  Category, Product, Images
  ○ e5-module-stock          [med ] ~4j  Stock, Warehouse, Movements
  ○ e6-module-cart           [high] ~5j  Cart, Order, Checkout
  ...

Utilise: make feature FEATURE=e4-module-category
```

### 2. Démarrer une feature

```bash
make feature FEATURE=e4-module-category
```

Ce qui se passe:
- ✅ Crée branche `feature/e4-module-category`
- ✅ Génère `.claude-feature-context.md` avec tout le contexte
- ✅ Affiche les instructions

### 3. Lancer Claude Code

```bash
make claude
```

Dans Claude Code, **colle le contenu de `.claude-feature-context.md`** qui contient:
- Stack technique
- Structure du projet
- Spec complète de la feature (entités, API, frontend)
- Critères d'acceptation
- Tests à écrire

### 4. Claude implémente tout

Claude génère **tous les fichiers** d'un coup:

```
backend/catalog/src/main/java/com/sportsaas/catalog/
├── domain/
│   ├── Category.java
│   ├── Product.java
│   └── ProductImage.java
├── repository/
│   ├── CategoryRepository.java
│   └── ProductRepository.java
├── api/
│   ├── dto/
│   │   ├── CreateCategoryRequest.java
│   │   └── CategoryResponse.java
│   ├── CategoryController.java
│   └── ProductController.java
├── service/
│   ├── CategoryService.java
│   └── ProductService.java
└── ...

frontend/src/app/features/catalog/
├── models/
├── services/
├── components/
└── pages/
```

### 5. Terminer la feature

```bash
make feature-done
```

Ce qui se passe:
- ✅ Build backend + frontend
- ✅ Lance les tests
- ✅ Commit formaté automatique
- ✅ Push + crée la PR
- ✅ Nettoie le contexte

---

## 🎫 Workflow Tickets (Pour petits fixes)

Pour les bugs ou petites tâches, utilise le workflow tickets:

```bash
make tickets                          # Voir les tickets
make ticket TICKET=E1-001 ID=42       # Prendre un ticket
make work                             # Claude + Commit + PR
make done                             # Terminer
```

---

## 📥 Import de données

### Depuis OpenProject (API)

```bash
make op-sync              # Import tickets + features depuis l'API
```

### Depuis CSV OpenProject

```bash
# Export ton backlog OpenProject en CSV, puis:
make import-openproject FILE=export.csv
```

### Depuis CSV manuel

```bash
make import-example                    # Génère des CSV exemples
make import-tickets FILE=tickets.csv   # Import tickets
make import-features FILE=features.csv # Import features
```

---

## 📊 Commandes principales

### Initialisation
| Commande | Description |
|----------|-------------|
| `make init` | Initialiser le projet |
| `make access` | Configurer GitHub + OpenProject |
| `make dev DEV=xxx` | Créer profil développeur |

### Features (recommandé)
| Commande | Description |
|----------|-------------|
| `make features` | Lister les features |
| `make feature FEATURE=xxx` | ⭐ Démarrer une feature |
| `make feature-done` | Terminer (build + PR) |

### Tickets
| Commande | Description |
|----------|-------------|
| `make tickets` | Lister les tickets |
| `make ticket TICKET=xxx ID=42` | Prendre un ticket |
| `make work` | Claude + Commit + PR |
| `make done` | Terminer le ticket |

### Développement
| Commande | Description |
|----------|-------------|
| `make up` | Démarrer Docker |
| `make down` | Arrêter Docker |
| `make backend` | Lancer Spring Boot |
| `make frontend` | Lancer Angular |
| `make test` | Lancer les tests |

### Claude
| Commande | Description |
|----------|-------------|
| `make claude` | Lancer Claude avec contexte |
| `make prompt TYPE=entity` | Afficher un prompt |

### Import
| Commande | Description |
|----------|-------------|
| `make import-openproject FILE=x.csv` | ⭐ CSV OpenProject → Features |
| `make op-sync` | Sync depuis API OpenProject |

---

## 🗂️ Structure du projet

```
sport-saas/
├── backend/                    # Spring Boot 3.3 (Java 17)
│   ├── app/                    # Application principale
│   ├── common/                 # Utilitaires partagés
│   ├── config/                 # Configuration
│   ├── auth/                   # Authentification
│   ├── tenant/                 # Multi-tenant
│   ├── catalog/                # Catalogue produits
│   ├── inventory/              # Gestion stocks
│   ├── order/                  # Commandes
│   ├── billing/                # Facturation
│   └── notification/           # Notifications
│
├── frontend/                   # Angular 17
│   └── src/app/
│       ├── core/               # Guards, interceptors
│       ├── shared/             # Composants partagés
│       └── features/           # Modules fonctionnels
│
├── team/                       # 👈 Gestion d'équipe
│   ├── features/               # Specs des features (YAML)
│   ├── tickets/                # Backlog tickets
│   ├── scripts/                # Scripts automatisation
│   ├── prompts/                # Prompts Claude
│   ├── devs/                   # Profils développeurs
│   ├── onboarding/             # Docs nouveaux arrivants
│   └── rules/                  # Standards de code
│
├── Makefile                    # Commandes principales
├── CLAUDE.md                   # Instructions pour Claude
└── docker-compose.yml          # Services Docker
```

---

## 🔧 Configuration requise

- **Java 17+**
- **Node.js 18+**
- **Docker & Docker Compose**
- **Git**
- **Claude Code** (recommandé)

---

## 💡 Tips

### Ordre recommandé des features

```
1. E1 - Configuration     (base Spring Boot)
2. E2 - Tenant            (multi-tenant)
3. E3 - Auth              (JWT, RBAC)
4. E4 - Catalogue         (Products, Categories)
5. E5 - Stock             (Inventory)
6. E6 - Orders            (Cart, Checkout)
7. E7 - Billing           (Invoices)
8. E11 - Frontend         (Angular UI)
9. E12 - Docker           (Deployment)
```

### Bonnes pratiques

1. **Une feature = une PR** - Pas de micro-commits
2. **Toujours partir de develop** - `make sync` après merge
3. **Utiliser le contexte Claude** - Copier `.claude-feature-context.md`
4. **Tester avant PR** - `make test`

---

## 🆘 Aide

```bash
make help     # Toutes les commandes
make status   # État du projet
make info     # Informations
make urls     # URLs utiles
```

**URLs:**
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:4200
- OpenProject: https://aam.openproject.com/
