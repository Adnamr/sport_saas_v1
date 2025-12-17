#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: complete-ticket.sh
# Description: Termine le ticket, push, crée la PR automatiquement
# Usage: ./team/scripts/complete-ticket.sh [DEV_ID]
# ═══════════════════════════════════════════════════════════════════

set -e

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

# Arguments
DEV_ID=${1:-$(whoami)}
DEV_FILE="team/devs/${DEV_ID}.yaml"
CONFIG_FILE="$HOME/.sport-saas/config"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    ✅ FINALISATION DU TICKET${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════
# 1. Récupérer le ticket en cours
# ═══════════════════════════════════════════════════════════════════

echo -e "${CYAN}[1/6] Vérification du ticket en cours...${NC}"

if [ ! -f "$DEV_FILE" ]; then
    echo -e "  ${RED}❌ Fichier développeur non trouvé: ${DEV_FILE}${NC}"
    echo ""
    echo -e "  ${YELLOW}📋 PROCHAINE ÉTAPE:${NC} ${GREEN}make dev DEV=${DEV_ID}${NC}"
    exit 1
fi

TICKET_ID=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "ticket_id:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")
OPENPROJECT_ID=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "openproject_id:" | head -1 | awk '{print $2}' || echo "0")
TICKET_TITLE=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "title:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")

if [ -z "$TICKET_ID" ] || [ "$TICKET_ID" = "null" ]; then
    echo -e "  ${RED}❌ Aucun ticket en cours pour ${DEV_ID}${NC}"
    echo ""
    echo -e "  ${YELLOW}📋 PROCHAINE ÉTAPE:${NC} ${GREEN}make ticket TICKET=xxx ID=xx${NC}"
    exit 1
fi

echo -e "  ${GREEN}✓${NC} Ticket: ${TICKET_ID} - ${TICKET_TITLE}"

# ═══════════════════════════════════════════════════════════════════
# 2. Vérifier la branche
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[2/6] Vérification de la branche...${NC}"

CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")

if [ -z "$CURRENT_BRANCH" ]; then
    echo -e "  ${RED}❌ Pas sur une branche Git${NC}"
    exit 1
fi

if [ "$CURRENT_BRANCH" = "develop" ] || [ "$CURRENT_BRANCH" = "main" ]; then
    echo -e "  ${RED}❌ Tu es sur ${CURRENT_BRANCH}, pas sur une branche feature${NC}"
    exit 1
fi

echo -e "  ${GREEN}✓${NC} Branche: ${CURRENT_BRANCH}"

# ═══════════════════════════════════════════════════════════════════
# 3. Gérer les changements non commités
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[3/6] Vérification des changements...${NC}"

if [ -n "$(git status --porcelain)" ]; then
    echo -e "  ${YELLOW}⚠️ Changements non commités:${NC}"
    git status --short | head -10
    echo ""
    
    read -p "  Message du commit: " COMMIT_MSG
    
    if [ -z "$COMMIT_MSG" ]; then
        COMMIT_MSG="work in progress"
    fi
    
    git add .
    
    # Format du commit
    TYPE="feat"
    if [[ "$COMMIT_MSG" == *"fix"* ]] || [[ "$COMMIT_MSG" == *"corr"* ]]; then
        TYPE="fix"
    elif [[ "$COMMIT_MSG" == *"test"* ]]; then
        TYPE="test"
    fi
    
    FORMATTED_MSG="${TYPE}(${TICKET_ID}): ${COMMIT_MSG} - Refs #${OPENPROJECT_ID}"
    git commit -m "$FORMATTED_MSG"
    echo -e "  ${GREEN}✓${NC} Commit: ${FORMATTED_MSG}"
else
    echo -e "  ${GREEN}✓${NC} Tous les changements sont commités"
fi

# ═══════════════════════════════════════════════════════════════════
# 4. Push la branche
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[4/6] Push de la branche...${NC}"

git push -u origin "$CURRENT_BRANCH" 2>&1 | head -5
echo -e "  ${GREEN}✓${NC} Branche pushée"

# ═══════════════════════════════════════════════════════════════════
# 5. Créer la PR automatiquement
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[5/6] Création de la Pull Request...${NC}"

PR_TITLE="${TICKET_ID}: ${TICKET_TITLE:-$TICKET_ID}"
PR_BODY="## Description
Ticket: ${TICKET_ID}
OpenProject: #${OPENPROJECT_ID}

## Changes
- $(git log --oneline develop..HEAD | head -5 | sed 's/^/- /')

## Checklist
- [ ] Tests passent
- [ ] Code review OK
- [ ] Documentation à jour

Refs #${OPENPROJECT_ID}"

# Vérifier si gh est disponible
if command -v gh &> /dev/null && gh auth status &> /dev/null 2>&1; then
    # Vérifier si une PR existe déjà
    EXISTING_PR=$(gh pr list --head "$CURRENT_BRANCH" --json number --jq '.[0].number' 2>/dev/null || echo "")
    
    if [ -n "$EXISTING_PR" ]; then
        PR_URL=$(gh pr view "$EXISTING_PR" --json url --jq '.url' 2>/dev/null)
        echo -e "  ${YELLOW}→${NC} PR existante: #${EXISTING_PR}"
        echo -e "  ${BLUE}${PR_URL}${NC}"
    else
        # Créer la PR
        PR_URL=$(gh pr create \
            --title "$PR_TITLE" \
            --body "$PR_BODY" \
            --base develop \
            --head "$CURRENT_BRANCH" \
            2>&1)
        
        if [ $? -eq 0 ]; then
            echo -e "  ${GREEN}✓${NC} PR créée!"
            echo -e "  ${BLUE}${PR_URL}${NC}"
        else
            echo -e "  ${RED}✗${NC} Erreur lors de la création de la PR"
            echo -e "  ${GRAY}${PR_URL}${NC}"
            
            # Fallback: afficher le lien manuel
            REMOTE_URL=$(git remote get-url origin 2>/dev/null | sed 's/\.git$//' | sed 's/git@github.com:/https:\/\/github.com\//')
            echo ""
            echo -e "  ${YELLOW}Crée la PR manuellement:${NC}"
            echo -e "  ${BLUE}${REMOTE_URL}/compare/develop...${CURRENT_BRANCH}?expand=1${NC}"
        fi
    fi
else
    echo -e "  ${YELLOW}⚠️${NC} GitHub CLI non disponible"
    
    # Afficher le lien manuel
    REMOTE_URL=$(git remote get-url origin 2>/dev/null | sed 's/\.git$//' | sed 's/git@github.com:/https:\/\/github.com\//')
    echo ""
    echo -e "  ${YELLOW}Crée la PR manuellement:${NC}"
    echo -e "  ${BLUE}${REMOTE_URL}/compare/develop...${CURRENT_BRANCH}?expand=1${NC}"
    
    echo ""
    echo -e "  ${YELLOW}📋 PROCHAINE ÉTAPE:${NC} ${GREEN}make access${NC} (pour configurer GitHub CLI)"
fi

# ═══════════════════════════════════════════════════════════════════
# 6. Mettre à jour OpenProject
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[6/6] Mise à jour OpenProject...${NC}"

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S")

# Mettre à jour le fichier dev
cat > "$DEV_FILE" << EOF
dev:
  id: "${DEV_ID}"
  name: "${DEV_ID}"
  email: "${DEV_ID}@example.com"
  slack: "@${DEV_ID}"
  role: "Développeur"
  joined_at: "$(date +%Y-%m-%d)"

current_ticket: null

claude_preferences:
  explanation_level: "normal"
  auto_generate_tests: true

history:
  - ticket_id: "${TICKET_ID}"
    completed_at: "${TIMESTAMP}"
    branch: "${CURRENT_BRANCH}"
    pr_url: "${PR_URL:-}"
EOF

echo -e "  ${GREEN}✓${NC} Fichier dev mis à jour"

# Mettre à jour OpenProject
if [ "$OPENPROJECT_ID" != "0" ] && [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
    if [ -n "$OPENPROJECT_TOKEN" ]; then
        # Passer en "In Review"
        ./team/scripts/openproject-api.sh review "$OPENPROJECT_ID" 2>/dev/null && \
            echo -e "  ${GREEN}✓${NC} OpenProject → In Review" || \
            echo -e "  ${YELLOW}⚠️${NC} Status non mis à jour"
        
        # Ajouter le lien PR en commentaire
        if [ -n "$PR_URL" ]; then
            ./team/scripts/openproject-api.sh comment "$OPENPROJECT_ID" "PR: ${PR_URL}" 2>/dev/null && \
                echo -e "  ${GREEN}✓${NC} Lien PR ajouté sur OpenProject" || true
        fi
    fi
fi

# Commit des fichiers de suivi
git add team/ 2>/dev/null || true
git commit -m "chore: ${DEV_ID} completes ticket ${TICKET_ID}" --allow-empty 2>/dev/null || true
git push origin "$CURRENT_BRANCH" 2>/dev/null || true

# ═══════════════════════════════════════════════════════════════════
# Résumé
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ TICKET ${TICKET_ID} TERMINÉ !${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
if [ -n "$PR_URL" ]; then
    echo -e "🔗 ${CYAN}PR:${NC} ${BLUE}${PR_URL}${NC}"
    echo ""
fi
echo -e "${YELLOW}📋 PROCHAINES ÉTAPES:${NC}"
echo ""
echo -e "   1. Attendre la review de la PR"
echo -e "   2. Après merge: ${GREEN}make sync${NC}"
echo -e "   3. Nouveau ticket: ${GREEN}make ticket TICKET=xxx ID=xx${NC}"
echo ""
