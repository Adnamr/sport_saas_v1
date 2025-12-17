#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: show-current-ticket.sh
# Description: Affiche le ticket en cours pour un développeur
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
GRAY='\033[0;90m'
NC='\033[0m'

DEV_ID=${1:-$(whoami)}
DEV_FILE="team/devs/${DEV_ID}.yaml"

if [ ! -f "$DEV_FILE" ]; then
    echo -e "${GRAY}Pas de profil développeur${NC}"
    exit 0
fi

# Extraire le ticket en cours
TICKET_ID=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "ticket_id:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")
TICKET_TITLE=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "title:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")
BRANCH=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "branch:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")

if [ -n "$TICKET_ID" ] && [ "$TICKET_ID" != "null" ]; then
    echo -e "${YELLOW}🎫 Ticket en cours:${NC} ${GREEN}${TICKET_ID}${NC} - ${TICKET_TITLE}"
    if [ -n "$BRANCH" ]; then
        echo -e "   ${GRAY}Branche: ${BRANCH}${NC}"
    fi
else
    echo -e "${GRAY}Aucun ticket en cours${NC}"
fi
