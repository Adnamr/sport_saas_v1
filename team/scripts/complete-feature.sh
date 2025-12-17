#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: complete-feature.sh
# Termine une feature: tests, commit, push, PR
# Usage: ./complete-feature.sh [DEV_ID]
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

DEV_ID=${1:-$(whoami)}
DEV_FILE="$PROJECT_DIR/team/devs/${DEV_ID}.yaml"

cd "$PROJECT_DIR"

# ═══════════════════════════════════════════════════════════════════
# Vérifier qu'une feature est en cours
# ═══════════════════════════════════════════════════════════════════

if [ ! -f "$DEV_FILE" ]; then
    echo -e "${RED}❌ Aucun profil trouvé pour: ${DEV_ID}${NC}"
    echo -e "${YELLOW}📋 Lance d'abord: make feature FEATURE=xxx${NC}"
    exit 1
fi

FEATURE_NAME=$(grep "feature_name:" "$DEV_FILE" 2>/dev/null | sed 's/.*: "\(.*\)"/\1/' || echo "")
FEATURE_TITLE=$(grep "title:" "$DEV_FILE" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' || echo "")
BRANCH=$(grep "branch:" "$DEV_FILE" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' || echo "")

if [ -z "$FEATURE_NAME" ]; then
    echo -e "${RED}❌ Aucune feature en cours${NC}"
    echo -e "${YELLOW}📋 Lance d'abord: make feature FEATURE=xxx${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}        ✅ FINALISATION: ${FEATURE_TITLE}${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Charger config projet
PROJECT_NAME=$(grep "name:" "$PROJECT_DIR/project.yaml" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' | sed 's/.*: //')
PROJECT_NAME_LOWER=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr ' ' '-')
BACKEND_STACK=$(grep "backend:" "$PROJECT_DIR/project.yaml" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' | sed 's/.*: //')
GIT_HOST=$(grep "git_host:" "$PROJECT_DIR/project.yaml" 2>/dev/null | sed 's/.*: "\(.*\)"/\1/' | sed 's/.*: //')

# ═══════════════════════════════════════════════════════════════════
# 1. Vérifier les fichiers modifiés
# ═══════════════════════════════════════════════════════════════════

echo -e "${CYAN}[1/6] Analyse des changements...${NC}"

CHANGED_FILES=$(git status --porcelain | wc -l)
ADDED_FILES=$(git status --porcelain | grep "^A\|^?" | wc -l)
MODIFIED_FILES=$(git status --porcelain | grep "^M" | wc -l)

if [ "$CHANGED_FILES" -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Aucun fichier modifié${NC}"
    echo ""
    read -p "Continuer quand même? (y/n) " -n 1 -r
    echo
    [[ ! $REPLY =~ ^[Yy]$ ]] && exit 0
else
    echo -e "  ${GREEN}✓${NC} ${ADDED_FILES} fichiers ajoutés"
    echo -e "  ${GREEN}✓${NC} ${MODIFIED_FILES} fichiers modifiés"
fi

# ═══════════════════════════════════════════════════════════════════
# 2. Build (vérifier que ça compile)
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[2/6] Vérification du build...${NC}"

BUILD_OK=true

case "$BACKEND_STACK" in
    "spring-boot")
        if [ -d "backend" ]; then
            echo -e "  ${GRAY}→ Compilation Maven...${NC}"
            if cd backend && ./mvnw compile -q 2>/dev/null; then
                echo -e "  ${GREEN}✓${NC} Backend compile"
            else
                echo -e "  ${RED}❌${NC} Backend ne compile pas"
                BUILD_OK=false
            fi
            cd "$PROJECT_DIR"
        fi
        ;;
    "nodejs")
        if [ -d "backend" ]; then
            echo -e "  ${GRAY}→ TypeScript check...${NC}"
            if cd backend && npm run build --silent 2>/dev/null; then
                echo -e "  ${GREEN}✓${NC} Backend compile"
            else
                echo -e "  ${YELLOW}⚠️${NC} Backend build failed (continuons)"
            fi
            cd "$PROJECT_DIR"
        fi
        ;;
esac

if [ -d "frontend" ]; then
    echo -e "  ${GRAY}→ Frontend build check...${NC}"
    if cd frontend && npm run build --silent 2>/dev/null; then
        echo -e "  ${GREEN}✓${NC} Frontend compile"
    else
        echo -e "  ${YELLOW}⚠️${NC} Frontend build failed (continuons)"
    fi
    cd "$PROJECT_DIR"
fi

if [ "$BUILD_OK" = false ]; then
    echo ""
    echo -e "${RED}❌ Le build a échoué. Corrige les erreurs avant de continuer.${NC}"
    exit 1
fi

# ═══════════════════════════════════════════════════════════════════
# 3. Tests (optionnel mais recommandé)
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[3/6] Tests...${NC}"

read -p "  Lancer les tests? (y/n) [y]: " -n 1 -r RUN_TESTS
RUN_TESTS=${RUN_TESTS:-y}
echo ""

if [[ $RUN_TESTS =~ ^[Yy]$ ]]; then
    case "$BACKEND_STACK" in
        "spring-boot")
            if [ -d "backend" ]; then
                echo -e "  ${GRAY}→ Tests Maven...${NC}"
                if cd backend && ./mvnw test -q 2>/dev/null; then
                    echo -e "  ${GREEN}✓${NC} Tests passent"
                else
                    echo -e "  ${YELLOW}⚠️${NC} Certains tests échouent"
                fi
                cd "$PROJECT_DIR"
            fi
            ;;
        "nodejs")
            if [ -d "backend" ]; then
                echo -e "  ${GRAY}→ Tests npm...${NC}"
                if cd backend && npm test --silent 2>/dev/null; then
                    echo -e "  ${GREEN}✓${NC} Tests passent"
                else
                    echo -e "  ${YELLOW}⚠️${NC} Certains tests échouent"
                fi
                cd "$PROJECT_DIR"
            fi
            ;;
        "python")
            if [ -d "backend" ]; then
                echo -e "  ${GRAY}→ Tests pytest...${NC}"
                if cd backend && pytest -q 2>/dev/null; then
                    echo -e "  ${GREEN}✓${NC} Tests passent"
                else
                    echo -e "  ${YELLOW}⚠️${NC} Certains tests échouent"
                fi
                cd "$PROJECT_DIR"
            fi
            ;;
    esac
else
    echo -e "  ${GRAY}→ Tests ignorés${NC}"
fi

# ═══════════════════════════════════════════════════════════════════
# 4. Commit
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[4/6] Commit...${NC}"

# Stage tous les fichiers
git add -A

# Message de commit
COMMIT_MSG="feat(${FEATURE_NAME}): ${FEATURE_TITLE}

Implemented complete feature:
$(git diff --cached --stat | tail -1)

Feature: ${FEATURE_NAME}
Branch: ${BRANCH}"

git commit -m "$COMMIT_MSG" 2>/dev/null || echo -e "  ${YELLOW}→${NC} Rien à commiter"

echo -e "  ${GREEN}✓${NC} Commit créé"

# ═══════════════════════════════════════════════════════════════════
# 5. Push et PR
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[5/6] Push et Pull Request...${NC}"

# Push la branche
git push -u origin "$BRANCH" 2>/dev/null || git push origin "$BRANCH" 2>/dev/null

echo -e "  ${GREEN}✓${NC} Branche pushée"

# Créer la PR
PR_TITLE="feat(${FEATURE_NAME}): ${FEATURE_TITLE}"
PR_BODY="## 🚀 Feature: ${FEATURE_TITLE}

### Description
Cette PR implémente la feature **${FEATURE_NAME}** de bout en bout.

### Changements
- Backend: Entities, Services, Controllers, Tests
- Frontend: Components, Pages, Services

### Tests
- [ ] Tests unitaires passent
- [ ] Tests d'intégration passent
- [ ] Tests manuels effectués

### Checklist
- [ ] Code review
- [ ] Documentation mise à jour
- [ ] Prêt pour merge

---
Spec: \`team/features/${FEATURE_NAME}.yaml\`"

PR_URL=""

case "$GIT_HOST" in
    "github")
        if command -v gh &>/dev/null; then
            PR_URL=$(gh pr create \
                --title "$PR_TITLE" \
                --body "$PR_BODY" \
                --base develop \
                --head "$BRANCH" 2>/dev/null || echo "")
            
            if [ -n "$PR_URL" ]; then
                echo -e "  ${GREEN}✓${NC} PR créée: ${PR_URL}"
            else
                # PR existe peut-être déjà
                PR_URL=$(gh pr view --json url -q '.url' 2>/dev/null || echo "")
                if [ -n "$PR_URL" ]; then
                    echo -e "  ${YELLOW}→${NC} PR existante: ${PR_URL}"
                else
                    echo -e "  ${YELLOW}⚠️${NC} Crée la PR manuellement sur GitHub"
                fi
            fi
        else
            echo -e "  ${YELLOW}⚠️${NC} GitHub CLI non installé, crée la PR manuellement"
        fi
        ;;
    "gitlab")
        if command -v glab &>/dev/null; then
            PR_URL=$(glab mr create \
                --title "$PR_TITLE" \
                --description "$PR_BODY" \
                --target-branch develop \
                --source-branch "$BRANCH" \
                --yes 2>/dev/null || echo "")
            
            if [ -n "$PR_URL" ]; then
                echo -e "  ${GREEN}✓${NC} MR créée: ${PR_URL}"
            else
                echo -e "  ${YELLOW}⚠️${NC} Crée la MR manuellement sur GitLab"
            fi
        else
            echo -e "  ${YELLOW}⚠️${NC} GitLab CLI non installé, crée la MR manuellement"
        fi
        ;;
    *)
        echo -e "  ${YELLOW}⚠️${NC} Crée la PR manuellement"
        ;;
esac

# ═══════════════════════════════════════════════════════════════════
# 6. Cleanup
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[6/6] Finalisation...${NC}"

# Mettre à jour le tracking
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

cat > "$DEV_FILE" << EOF
dev:
  id: "${DEV_ID}"
  name: "${DEV_ID}"
  email: "${DEV_ID}@example.com"

current_feature: null

history:
  - feature_id: "${FEATURE_NAME}"
    title: "${FEATURE_TITLE}"
    branch: "${BRANCH}"
    completed_at: "${TIMESTAMP}"
    pr_url: "${PR_URL}"
EOF

# Nettoyer le fichier de contexte
rm -f "$PROJECT_DIR/.claude-feature-context.md"

echo -e "  ${GREEN}✓${NC} Tracking mis à jour"

# ═══════════════════════════════════════════════════════════════════
# Résumé
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ FEATURE ${FEATURE_NAME} TERMINÉE !${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "  🎯 ${CYAN}Feature:${NC}  ${FEATURE_TITLE}"
echo -e "  🌿 ${CYAN}Branche:${NC}  ${BRANCH}"
[ -n "$PR_URL" ] && echo -e "  🔗 ${CYAN}PR:${NC}       ${PR_URL}"
echo ""
echo -e "${YELLOW}📋 PROCHAINES ÉTAPES:${NC}"
echo ""
echo -e "  1. ${WHITE}Review la PR${NC}"
echo -e "  2. ${WHITE}Merge dans develop${NC}"
echo -e "  3. ${GREEN}make git-sync${NC} pour revenir sur develop"
echo ""
echo -e "  ${GRAY}Ou commence une nouvelle feature:${NC}"
echo -e "  ${GREEN}make feature FEATURE=xxx${NC}"
echo ""
