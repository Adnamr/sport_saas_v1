#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: start-feature.sh
# Démarre une feature complète de bout en bout
# Usage: ./start-feature.sh <FEATURE_NAME> [DEV_ID]
# ═══════════════════════════════════════════════════════════════════

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"

FEATURE_NAME=$1
DEV_ID=${2:-$(whoami)}

cd "$PROJECT_DIR"

# ═══════════════════════════════════════════════════════════════════
# Validation
# ═══════════════════════════════════════════════════════════════════

if [ -z "$FEATURE_NAME" ]; then
    echo ""
    echo -e "${RED}❌ Usage: make feature FEATURE=xxx${NC}"
    echo ""
    echo -e "${YELLOW}Features disponibles:${NC}"
    echo ""
    for f in team/features/*.yaml; do
        [ -f "$f" ] || continue
        [[ "$(basename "$f")" == "_template.yaml" ]] && continue
        FNAME=$(basename "$f" .yaml)
        FDESC=$(grep "^  name:" "$f" 2>/dev/null | head -1 | sed 's/.*: "//' | sed 's/"//')
        echo -e "  ${CYAN}${FNAME}${NC} - ${FDESC}"
    done
    echo ""
    echo -e "${GRAY}Pour créer une nouvelle feature:${NC}"
    echo -e "  ${GREEN}cp team/features/_template.yaml team/features/ma-feature.yaml${NC}"
    echo ""
    exit 1
fi

FEATURE_FILE="$PROJECT_DIR/team/features/${FEATURE_NAME}.yaml"

if [ ! -f "$FEATURE_FILE" ]; then
    echo -e "${RED}❌ Feature non trouvée: ${FEATURE_NAME}${NC}"
    echo ""
    echo -e "${YELLOW}Fichier attendu:${NC} team/features/${FEATURE_NAME}.yaml"
    echo ""
    echo -e "Créer depuis le template:"
    echo -e "  ${GREEN}cp team/features/_template.yaml team/features/${FEATURE_NAME}.yaml${NC}"
    exit 1
fi

# Charger les infos de la feature
FEATURE_ID=$(grep "^  id:" "$FEATURE_FILE" 2>/dev/null | head -1 | sed 's/.*: "//' | sed 's/"//')
FEATURE_TITLE=$(grep "^  name:" "$FEATURE_FILE" 2>/dev/null | head -1 | sed 's/.*: "//' | sed 's/"//')
FEATURE_PRIORITY=$(grep "^  priority:" "$FEATURE_FILE" 2>/dev/null | head -1 | sed 's/.*: "//' | sed 's/"//')

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}        🚀 FEATURE: ${FEATURE_TITLE}${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════
# 1. Vérifier les accès
# ═══════════════════════════════════════════════════════════════════

echo -e "${CYAN}[1/5] Vérification des accès...${NC}"

if ! "$SCRIPT_DIR/check-access.sh" --quiet 2>/dev/null; then
    echo -e "${RED}❌ Accès non configurés${NC}"
    echo -e "${YELLOW}📋 Lance d'abord: make access${NC}"
    exit 1
fi

echo -e "  ${GREEN}✓${NC} Accès OK"

# ═══════════════════════════════════════════════════════════════════
# 2. Sync et créer la branche
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[2/5] Préparation Git...${NC}"

# Sauvegarder les changements en cours
git stash -q 2>/dev/null || true

# Sync avec develop
git checkout develop 2>/dev/null || git checkout -b develop 2>/dev/null || true
git pull origin develop 2>/dev/null || true

echo -e "  ${GREEN}✓${NC} Develop à jour"

# Créer la branche feature
BRANCH_NAME="feature/${FEATURE_NAME}"

if git show-ref --verify --quiet "refs/heads/$BRANCH_NAME" 2>/dev/null; then
    git checkout "$BRANCH_NAME"
    echo -e "  ${YELLOW}→${NC} Branche existante: ${BRANCH_NAME}"
else
    git checkout -b "$BRANCH_NAME"
    echo -e "  ${GREEN}✓${NC} Nouvelle branche: ${BRANCH_NAME}"
fi

# ═══════════════════════════════════════════════════════════════════
# 3. Générer le prompt complet
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[3/5] Génération du contexte Claude...${NC}"

# Charger les infos projet
PROJECT_NAME=$(grep "name:" "$PROJECT_DIR/project.yaml" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' | sed 's/.*: //')
BACKEND_STACK=$(grep "backend:" "$PROJECT_DIR/project.yaml" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' | sed 's/.*: //')
FRONTEND_STACK=$(grep "frontend:" "$PROJECT_DIR/project.yaml" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' | sed 's/.*: //')
DATABASE=$(grep "database:" "$PROJECT_DIR/project.yaml" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' | sed 's/.*: //')

# Créer le fichier de contexte pour Claude
CONTEXT_FILE="$PROJECT_DIR/.claude-feature-context.md"

cat > "$CONTEXT_FILE" << CONTEXT
# 🚀 FEATURE À IMPLÉMENTER: ${FEATURE_TITLE}

## Contexte Projet
- **Projet**: ${PROJECT_NAME}
- **Backend**: ${BACKEND_STACK}
- **Frontend**: ${FRONTEND_STACK}
- **Database**: ${DATABASE}
- **Branche**: ${BRANCH_NAME}

---

## 📋 Spécification Complète

$(cat "$FEATURE_FILE")

---

## 🎯 INSTRUCTIONS

Tu dois implémenter cette feature **DE BOUT EN BOUT** en une seule session.

### Backend (dans l'ordre)
1. **Entities** - Créer les entités JPA avec validations
2. **Repositories** - Interfaces + méthodes custom si nécessaire
3. **DTOs** - Request/Response objects
4. **Services** - Logique métier complète
5. **Controllers** - Endpoints REST
6. **Tests** - Unitaires (Service) + Intégration (Controller)

### Frontend (dans l'ordre)
1. **Models** - Interfaces TypeScript
2. **Services** - Appels API
3. **Components** - UI components
4. **Pages** - List, Detail, Form
5. **Routing** - Configuration des routes
6. **Tests** - Tests composants si possible

### Qualité attendue
- Code propre et bien structuré
- Validations complètes
- Gestion d'erreurs
- Tests couvrant les cas principaux
- Documentation si nécessaire

### Output
Génère **TOUS** les fichiers nécessaires, prêts à compiler et fonctionnels.

---

## 📁 Structure attendue

\`\`\`
backend/src/main/java/com/.../
├── domain/
│   └── [Entity].java
├── repository/
│   └── [Entity]Repository.java
├── service/
│   ├── [Entity]Service.java
│   └── impl/[Entity]ServiceImpl.java
├── api/
│   ├── [Entity]Controller.java
│   └── dto/
│       ├── [Entity]DTO.java
│       ├── Create[Entity]Request.java
│       └── Update[Entity]Request.java
└── exception/
    └── [Entity]NotFoundException.java

backend/src/test/java/com/.../
├── service/
│   └── [Entity]ServiceTest.java
└── api/
    └── [Entity]ControllerIntegrationTest.java

frontend/src/app/
└── features/[module]/
    ├── models/
    │   └── [entity].model.ts
    ├── services/
    │   └── [entity].service.ts
    ├── components/
    │   └── [component]/
    └── pages/
        ├── [entity]-list/
        ├── [entity]-detail/
        └── [entity]-form/
\`\`\`

---

**COMMENCE MAINTENANT. Génère tous les fichiers un par un.**
CONTEXT

echo -e "  ${GREEN}✓${NC} Contexte généré: .claude-feature-context.md"

# ═══════════════════════════════════════════════════════════════════
# 4. Mettre à jour le tracking
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[4/5] Mise à jour du tracking...${NC}"

DEV_FILE="$PROJECT_DIR/team/devs/${DEV_ID}.yaml"
mkdir -p "$(dirname "$DEV_FILE")"

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

cat > "$DEV_FILE" << EOF
dev:
  id: "${DEV_ID}"
  name: "${DEV_ID}"
  email: "${DEV_ID}@example.com"

current_feature:
  feature_id: "${FEATURE_ID}"
  feature_name: "${FEATURE_NAME}"
  title: "${FEATURE_TITLE}"
  branch: "${BRANCH_NAME}"
  started_at: "${TIMESTAMP}"
  status: "in_progress"

history: []
EOF

echo -e "  ${GREEN}✓${NC} Tracking mis à jour"

# ═══════════════════════════════════════════════════════════════════
# 5. Afficher les instructions
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[5/5] Prêt !${NC}"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ FEATURE ${FEATURE_NAME} PRÊTE !${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "  🎯 ${CYAN}Feature:${NC}  ${FEATURE_TITLE}"
echo -e "  🌿 ${CYAN}Branche:${NC}  ${BRANCH_NAME}"
echo -e "  📋 ${CYAN}Spec:${NC}     team/features/${FEATURE_NAME}.yaml"
echo -e "  📝 ${CYAN}Contexte:${NC} .claude-feature-context.md"
echo ""
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}                    PROCHAINES ÉTAPES${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "  ${WHITE}Option 1: Claude Code (recommandé)${NC}"
echo -e "  ${GREEN}make claude${NC}"
echo -e "  ${GRAY}Puis copie le contenu de .claude-feature-context.md${NC}"
echo ""
echo -e "  ${WHITE}Option 2: Claude Web${NC}"
echo -e "  ${GRAY}Copie le contenu de .claude-feature-context.md dans claude.ai${NC}"
echo ""
echo -e "  ${WHITE}Une fois terminé:${NC}"
echo -e "  ${GREEN}make feature-done${NC}"
echo ""
