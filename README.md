# 🏀 Sport Equipment SaaS

Plateforme SaaS multi-tenant de gestion d'équipements sportifs.

## 🛠️ Stack technique

| Layer | Technologies |
|-------|--------------|
| **Backend** | Java 17, Spring Boot 3.3, PostgreSQL, Flyway |
| **Frontend** | Angular 17, TypeScript, Tailwind CSS |
| **Infra** | Docker, GitHub Actions |
| **Outils** | OpenProject, Claude Code |

## ⚡ Démarrage rapide

```bash
# 1. Cloner
git clone <repo-url>
cd sport-saas

# 2. Initialiser (obligatoire)
make init
make access        # Configurer GitHub + OpenProject
make dev DEV=xxx   # Créer ton profil

# 3. Lancer
make up            # Docker
make backend       # Terminal 1
make frontend      # Terminal 2
```

📖 **Guide complet:** [GETTING-STARTED.md](./GETTING-STARTED.md)

## 🎯 Workflow Feature-Based

Ce projet utilise un workflow optimisé pour Claude Code:

```bash
make features                        # Voir les features
make feature FEATURE=e4-module-category  # Démarrer
make claude                          # Lancer Claude
make feature-done                    # Terminer (PR auto)
```

**Avantage:** 1 feature = 1 PR (au lieu de 10+ tickets)

## 📋 Features disponibles

| ID | Feature | Tickets | Estimation |
|----|---------|---------|------------|
| E1 | Configuration | 5 | ~2 jours |
| E2 | Multi-tenant | 7 | ~3 jours |
| E3 | Auth & RBAC | 12 | ~5 jours |
| E4 | Catalogue | 8 | ~4 jours |
| E5 | Stock | 8 | ~4 jours |
| E6 | Commandes | 9 | ~5 jours |
| E7 | Facturation | 6 | ~3 jours |
| E11 | Frontend | 11 | ~7 jours |
| E12 | Docker | 3 | ~1 jour |

## 🗂️ Structure

```
sport-saas/
├── backend/          # Spring Boot (modules: auth, tenant, catalog, ...)
├── frontend/         # Angular
├── team/             # Gestion équipe & features
│   ├── features/     # Specs YAML des features
│   ├── scripts/      # Automatisation
│   └── devs/         # Profils développeurs
├── Makefile          # Commandes principales
└── CLAUDE.md         # Instructions Claude
```

## 📊 Commandes

```bash
make help           # Aide complète

# Initialisation
make init           # Initialiser le projet
make access         # Configurer accès
make dev DEV=xxx    # Profil développeur

# Features (recommandé)
make features       # Lister
make feature FEATURE=xxx   # Démarrer
make feature-done   # Terminer

# Développement
make up / down      # Docker
make backend        # Spring Boot
make frontend       # Angular
make test           # Tests
```

## 🔗 Liens

- **Swagger:** http://localhost:8080/swagger-ui.html
- **Frontend:** http://localhost:4200
- **OpenProject:** https://aam.openproject.com/

## 📚 Documentation

- [GETTING-STARTED.md](./GETTING-STARTED.md) - Guide démarrage
- [CLAUDE.md](./CLAUDE.md) - Instructions Claude
- [team/rules/WORKFLOW.md](./team/rules/WORKFLOW.md) - Workflow détaillé
- [team/rules/code-standards.md](./team/rules/code-standards.md) - Standards de code
