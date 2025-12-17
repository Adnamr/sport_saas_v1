#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: work.sh
# Description: Workflow complet: Claude Code → Commit → PR → OpenProject
# Usage: ./team/scripts/work.sh [DEV_ID]
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

DEV_ID=${1:-$(whoami)}
DEV_FILE="team/devs/${DEV_ID}.yaml"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    🔄 WORKFLOW COMPLET${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════
# Vérifier le ticket en cours
# ═══════════════════════════════════════════════════════════════════

if [ ! -f "$DEV_FILE" ]; then
    echo -e "${RED}❌ Pas de profil développeur${NC}"
    echo ""
    echo -e "${YELLOW}📋 PROCHAINE ÉTAPE:${NC} ${GREEN}make dev DEV=${DEV_ID}${NC}"
    exit 1
fi

TICKET_ID=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "ticket_id:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")

if [ -z "$TICKET_ID" ] || [ "$TICKET_ID" = "null" ]; then
    echo -e "${RED}❌ Aucun ticket en cours${NC}"
    echo ""
    echo -e "${YELLOW}📋 PROCHAINE ÉTAPE:${NC} ${GREEN}make ticket TICKET=xxx ID=xx${NC}"
    exit 1
fi

TICKET_TITLE=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "title:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null)

echo -e "${CYAN}Ticket:${NC}  ${YELLOW}${TICKET_ID}${NC} - ${TICKET_TITLE}"
echo -e "${CYAN}Branche:${NC} ${CURRENT_BRANCH}"
echo ""

# ═══════════════════════════════════════════════════════════════════
# ÉTAPE 1: Claude Code
# ═══════════════════════════════════════════════════════════════════

echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   ÉTAPE 1/3: DÉVELOPPEMENT AVEC CLAUDE CODE${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Vérifier si Claude CLI est disponible
if command -v claude &> /dev/null; then
    echo -e "${YELLOW}Lancement de Claude Code...${NC}"
    echo -e "${GRAY}(Tape 'exit' ou Ctrl+D pour terminer et continuer)${NC}"
    echo ""
    
    # Lancer Claude
    claude || true
    
    echo ""
else
    echo -e "${YELLOW}Claude CLI non installé.${NC}"
    echo ""
    echo -e "Options:"
    echo -e "  1. Installer: ${CYAN}npm install -g @anthropic-ai/claude-cli${NC}"
    echo -e "  2. Utiliser Claude.ai avec: ${CYAN}make prompt TYPE=start-ticket${NC}"
    echo ""
    echo -e "${GRAY}Tape le code manuellement, puis appuie sur Entrée pour continuer...${NC}"
    read -p ""
fi

# ═══════════════════════════════════════════════════════════════════
# ÉTAPE 2: Vérifier les changements
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   ÉTAPE 2/3: VÉRIFICATION DES CHANGEMENTS${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

if [ -z "$(git status --porcelain)" ]; then
    echo -e "${YELLOW}⚠️ Aucun changement détecté.${NC}"
    echo ""
    
    read -p "Continuer quand même? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo ""
        echo -e "${YELLOW}📋 PROCHAINE ÉTAPE:${NC} Modifie des fichiers, puis relance ${GREEN}make work${NC}"
        exit 0
    fi
else
    echo -e "${CYAN}Fichiers modifiés:${NC}"
    git status --short
    echo ""
    
    echo -e "${YELLOW}Que veux-tu faire?${NC}"
    echo -e "  1. Continuer et créer la PR"
    echo -e "  2. Retourner dans Claude Code"
    echo -e "  3. Annuler"
    echo ""
    read -p "Choix [1]: " CHOICE
    CHOICE=${CHOICE:-1}
    
    case "$CHOICE" in
        2)
            echo ""
            exec ./team/scripts/work.sh "$DEV_ID"
            ;;
        3)
            echo ""
            echo -e "${YELLOW}Annulé. Tes changements sont toujours là.${NC}"
            echo -e "${YELLOW}📋 PROCHAINE ÉTAPE:${NC} ${GREEN}make work${NC} quand tu es prêt"
            exit 0
            ;;
    esac
fi

# ═══════════════════════════════════════════════════════════════════
# ÉTAPE 3: Terminer (commit + PR + OpenProject)
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   ÉTAPE 3/3: FINALISATION${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Lancer complete-ticket.sh qui fait tout
./team/scripts/complete-ticket.sh "$DEV_ID"
