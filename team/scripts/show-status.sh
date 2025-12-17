#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════
# Script: show-status.sh
# Description: Affiche l'état complet du projet
# ═══════════════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    📊 STATUS DU PROJET${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# Git status
echo ""
echo -e "${CYAN}🌿 GIT${NC}"
BRANCH=$(git branch --show-current 2>/dev/null || echo "N/A")
echo -e "   Branche: ${YELLOW}${BRANCH}${NC}"

if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
    CHANGES=$(git status --porcelain | wc -l | tr -d ' ')
    echo -e "   Changements: ${RED}${CHANGES} fichier(s) modifié(s)${NC}"
else
    echo -e "   Changements: ${GREEN}Aucun${NC}"
fi

# Docker status
echo ""
echo -e "${CYAN}🐳 DOCKER${NC}"
if docker info &> /dev/null; then
    POSTGRES=$(docker ps --filter "name=sport-saas-db" --format "{{.Status}}" 2>/dev/null)
    MAILHOG=$(docker ps --filter "name=sport-saas-mail" --format "{{.Status}}" 2>/dev/null)
    
    if [ -n "$POSTGRES" ]; then
        echo -e "   PostgreSQL: ${GREEN}Running${NC} ${GRAY}(${POSTGRES})${NC}"
    else
        echo -e "   PostgreSQL: ${RED}Stopped${NC}"
    fi
    
    if [ -n "$MAILHOG" ]; then
        echo -e "   Mailhog:    ${GREEN}Running${NC} ${GRAY}(${MAILHOG})${NC}"
    else
        echo -e "   Mailhog:    ${RED}Stopped${NC}"
    fi
else
    echo -e "   ${RED}Docker n'est pas démarré${NC}"
fi

# Tickets en cours
echo ""
echo -e "${CYAN}🎫 TICKETS EN COURS${NC}"

CURRENT_FILE="team/tickets/current.yaml"
if [ -f "$CURRENT_FILE" ]; then
    # Compter les tickets in_progress
    IN_PROGRESS=$(grep -A 100 "^in_progress:" "$CURRENT_FILE" 2>/dev/null | grep "ticket_id:" | head -10)
    
    if [ -z "$IN_PROGRESS" ]; then
        echo -e "   ${GRAY}Aucun ticket en cours${NC}"
    else
        echo "$IN_PROGRESS" | while read -r line; do
            TICKET=$(echo "$line" | grep -o '"[^"]*"' | tr -d '"')
            echo -e "   ${YELLOW}→${NC} $TICKET"
        done
    fi
fi

# Développeurs actifs
echo ""
echo -e "${CYAN}👥 DÉVELOPPEURS${NC}"

for dev_file in team/devs/*.yaml; do
    if [ -f "$dev_file" ] && [[ "$dev_file" != *"_template"* ]]; then
        DEV_ID=$(basename "$dev_file" .yaml)
        CURRENT_TICKET=$(grep -A 1 "^current_ticket:" "$dev_file" 2>/dev/null | grep "ticket_id:" | grep -o '"[^"]*"' | tr -d '"')
        
        if [ -n "$CURRENT_TICKET" ] && [ "$CURRENT_TICKET" != "null" ]; then
            echo -e "   ${GREEN}●${NC} ${DEV_ID}: ${YELLOW}${CURRENT_TICKET}${NC}"
        else
            echo -e "   ${GRAY}○${NC} ${DEV_ID}: ${GRAY}libre${NC}"
        fi
    fi
done

# Tickets prêts
echo ""
echo -e "${CYAN}📋 TICKETS PRÊTS (à prendre)${NC}"

if [ -f "$CURRENT_FILE" ]; then
    READY=$(grep -A 100 "^ready:" "$CURRENT_FILE" 2>/dev/null | grep "ticket_id:" | head -5)
    
    if [ -z "$READY" ]; then
        echo -e "   ${GRAY}Aucun ticket prêt${NC}"
    else
        echo "$READY" | while read -r line; do
            TICKET=$(echo "$line" | grep -o '"[^"]*"' | tr -d '"')
            echo -e "   ${GREEN}•${NC} $TICKET"
        done
        
        TOTAL=$(grep -A 100 "^ready:" "$CURRENT_FILE" 2>/dev/null | grep "ticket_id:" | wc -l | tr -d ' ')
        if [ "$TOTAL" -gt 5 ]; then
            echo -e "   ${GRAY}... et $((TOTAL - 5)) autres${NC}"
        fi
    fi
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "Commandes: ${YELLOW}make tickets${NC} (liste complète) | ${YELLOW}make ticket TICKET=xxx${NC} (prendre)"
echo ""
