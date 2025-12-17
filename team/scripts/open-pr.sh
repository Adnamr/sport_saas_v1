#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: open-pr.sh
# Description: Ouvre la page de création de PR sur GitHub
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

BRANCH=$(git branch --show-current)

if [ "$BRANCH" = "develop" ] || [ "$BRANCH" = "main" ] || [ "$BRANCH" = "master" ]; then
    echo -e "${RED}❌ Tu es sur ${BRANCH}, pas sur une branche de feature${NC}"
    exit 1
fi

# Générer le lien PR
REMOTE_URL=$(git remote get-url origin 2>/dev/null | sed 's/\.git$//' | sed 's/git@github.com:/https:\/\/github.com\//')
PR_URL="${REMOTE_URL}/compare/develop...${BRANCH}?expand=1"

echo ""
echo -e "${CYAN}🔗 URL de la PR:${NC}"
echo -e "   ${BLUE}${PR_URL}${NC}"
echo ""

# Essayer d'ouvrir dans le navigateur
if command -v xdg-open &> /dev/null; then
    xdg-open "$PR_URL" 2>/dev/null
    echo -e "${GREEN}✓ Ouvert dans le navigateur${NC}"
elif command -v open &> /dev/null; then
    open "$PR_URL" 2>/dev/null
    echo -e "${GREEN}✓ Ouvert dans le navigateur${NC}"
else
    echo -e "${YELLOW}Copie l'URL ci-dessus et ouvre-la dans ton navigateur${NC}"
fi
