#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════
# Script: list-tickets.sh
# Description: Liste les tickets disponibles
# ═══════════════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

CURRENT_FILE="team/tickets/current.yaml"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    📋 TICKETS DISPONIBLES${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

if [ ! -f "$CURRENT_FILE" ]; then
    echo -e "${RED}❌ Fichier $CURRENT_FILE non trouvé${NC}"
    exit 1
fi

# Tickets prêts
echo ""
echo -e "${CYAN}✅ PRÊTS (dépendances OK)${NC}"
echo ""

# Parser le YAML de manière simple
IN_READY=false
TICKET_ID=""
OPENPROJECT_ID=""
TITLE=""
SPRINT=""
HOURS=""

while IFS= read -r line; do
    # Détecter la section ready:
    if [[ "$line" == "ready:"* ]]; then
        IN_READY=true
        continue
    fi
    
    # Sortir de la section si on rencontre une autre section
    if [[ "$line" =~ ^[a-z_]+:$ ]] && [[ "$line" != "ready:" ]]; then
        IN_READY=false
    fi
    
    if [ "$IN_READY" = true ]; then
        # Parser les champs
        if [[ "$line" =~ ticket_id:\ *\"([^\"]+)\" ]]; then
            TICKET_ID="${BASH_REMATCH[1]}"
        elif [[ "$line" =~ openproject_id:\ *([0-9]+) ]]; then
            OPENPROJECT_ID="${BASH_REMATCH[1]}"
        elif [[ "$line" =~ title:\ *\"([^\"]+)\" ]]; then
            TITLE="${BASH_REMATCH[1]}"
        elif [[ "$line" =~ sprint:\ *\"([^\"]+)\" ]]; then
            SPRINT="${BASH_REMATCH[1]}"
        elif [[ "$line" =~ estimated_hours:\ *([0-9]+) ]]; then
            HOURS="${BASH_REMATCH[1]}"
            
            # Afficher le ticket complet
            printf "   ${GREEN}%-10s${NC} ${GRAY}#%-3s${NC} %-35s ${CYAN}%s${NC} ${GRAY}(%sh)${NC}\n" \
                "$TICKET_ID" "$OPENPROJECT_ID" "$TITLE" "$SPRINT" "$HOURS"
            
            # Reset
            TICKET_ID=""
            OPENPROJECT_ID=""
            TITLE=""
            SPRINT=""
            HOURS=""
        fi
    fi
done < "$CURRENT_FILE"

# Tickets en cours
echo ""
echo -e "${CYAN}🔄 EN COURS${NC}"
echo ""

IN_PROGRESS=false
while IFS= read -r line; do
    if [[ "$line" == "in_progress:"* ]]; then
        IN_PROGRESS=true
        continue
    fi
    
    if [[ "$line" =~ ^[a-z_]+:$ ]] && [[ "$line" != "in_progress:" ]]; then
        IN_PROGRESS=false
    fi
    
    if [ "$IN_PROGRESS" = true ]; then
        if [[ "$line" =~ ticket_id:\ *\"([^\"]+)\" ]]; then
            TICKET_ID="${BASH_REMATCH[1]}"
        elif [[ "$line" =~ dev:\ *\"([^\"]+)\" ]]; then
            DEV="${BASH_REMATCH[1]}"
            printf "   ${YELLOW}%-10s${NC} → ${BLUE}%s${NC}\n" "$TICKET_ID" "$DEV"
            TICKET_ID=""
            DEV=""
        fi
    fi
done < "$CURRENT_FILE"

# Si aucun en cours
if [ "$IN_PROGRESS" = false ] || [ -z "$(grep -A 5 "^in_progress:" "$CURRENT_FILE" | grep "ticket_id")" ]; then
    echo -e "   ${GRAY}Aucun ticket en cours${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "Pour prendre un ticket: ${YELLOW}make ticket TICKET=E1-001 ID=1${NC}"
echo ""
