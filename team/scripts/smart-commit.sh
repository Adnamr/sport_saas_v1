#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: smart-commit.sh
# Description: Commit avec format automatique basé sur la branche
# Usage: ./team/scripts/smart-commit.sh "message"
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

MSG="$1"

if [ -z "$MSG" ]; then
    echo -e "${RED}❌ Usage: make commit MSG=\"description\"${NC}"
    exit 1
fi

# Récupérer les infos de la branche
BRANCH=$(git branch --show-current)

# Extraire TICKET_ID et OPENPROJECT_ID de la branche
# Format: feature/42-E1-002-description
if [[ "$BRANCH" =~ ^feature/([0-9]+)-([A-Z0-9-]+) ]]; then
    OPENPROJECT_ID="${BASH_REMATCH[1]}"
    TICKET_ID="${BASH_REMATCH[2]}"
elif [[ "$BRANCH" =~ ^fix/([0-9]+)-([A-Z0-9-]+) ]]; then
    OPENPROJECT_ID="${BASH_REMATCH[1]}"
    TICKET_ID="${BASH_REMATCH[2]}"
else
    # Pas de format reconnu, commit simple
    echo -e "${YELLOW}⚠️ Branche non reconnue, commit simple${NC}"
    git add .
    git commit -m "$MSG"
    echo -e "${GREEN}✓ Commit effectué${NC}"
    exit 0
fi

# Déterminer le type de commit
TYPE="feat"
if [[ "$MSG" == *"fix"* ]] || [[ "$MSG" == *"corr"* ]] || [[ "$MSG" == *"bug"* ]]; then
    TYPE="fix"
elif [[ "$MSG" == *"test"* ]]; then
    TYPE="test"
elif [[ "$MSG" == *"doc"* ]]; then
    TYPE="docs"
elif [[ "$MSG" == *"refactor"* ]]; then
    TYPE="refactor"
elif [[ "$MSG" == *"style"* ]] || [[ "$MSG" == *"format"* ]]; then
    TYPE="style"
elif [[ "$MSG" == *"chore"* ]] || [[ "$MSG" == *"config"* ]]; then
    TYPE="chore"
fi

# Construire le message formaté
FORMATTED_MSG="${TYPE}(${TICKET_ID}): ${MSG} - Refs #${OPENPROJECT_ID}"

echo -e "${CYAN}📝 Commit:${NC} ${FORMATTED_MSG}"
echo ""

# Vérifier s'il y a des changements
if [ -z "$(git status --porcelain)" ]; then
    echo -e "${YELLOW}⚠️ Rien à commiter${NC}"
    exit 0
fi

# Afficher les fichiers modifiés
echo -e "${CYAN}Fichiers:${NC}"
git status --short
echo ""

# Commit
git add .
git commit -m "$FORMATTED_MSG"

echo ""
echo -e "${GREEN}✓ Commit effectué !${NC}"
echo ""
echo -e "Pour push: ${YELLOW}make push${NC}"
