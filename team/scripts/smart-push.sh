#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: smart-push.sh
# Description: Push la branche et affiche le lien PR
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

BRANCH=$(git branch --show-current)

if [ "$BRANCH" = "develop" ] || [ "$BRANCH" = "main" ] || [ "$BRANCH" = "master" ]; then
    echo -e "${RED}❌ Ne pas push directement sur ${BRANCH}${NC}"
    echo -e "   Crée une branche: ${YELLOW}make ticket TICKET=xxx${NC}"
    exit 1
fi

echo -e "${CYAN}🚀 Push de la branche ${BRANCH}...${NC}"
echo ""

# Push
git push -u origin "$BRANCH" 2>&1

echo ""
echo -e "${GREEN}✓ Branche pushée !${NC}"

# Générer le lien PR
REMOTE_URL=$(git remote get-url origin 2>/dev/null | sed 's/\.git$//' | sed 's/git@github.com:/https:\/\/github.com\//')

if [ -n "$REMOTE_URL" ]; then
    PR_URL="${REMOTE_URL}/compare/develop...${BRANCH}?expand=1"
    
    echo ""
    echo -e "${CYAN}🔗 Créer la PR:${NC}"
    echo -e "   ${BLUE}${PR_URL}${NC}"
    echo ""
fi
