#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: launch-claude.sh
# Description: Lance Claude Code avec le contexte du projet
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

DEV_ID=${1:-$(whoami)}
DEV_FILE="team/devs/${DEV_ID}.yaml"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    🤖 LANCEMENT CLAUDE CODE${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Vérifier le ticket en cours
TICKET_ID=""
if [ -f "$DEV_FILE" ]; then
    TICKET_ID=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "ticket_id:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")
fi

if [ -n "$TICKET_ID" ] && [ "$TICKET_ID" != "null" ]; then
    echo -e "${CYAN}📋 Ticket en cours:${NC} ${YELLOW}${TICKET_ID}${NC}"
else
    echo -e "${YELLOW}⚠️ Aucun ticket en cours${NC}"
    echo -e "   Prends un ticket: ${GREEN}make ticket TICKET=xxx${NC}"
fi

echo ""
echo -e "${CYAN}📄 Fichiers de contexte:${NC}"
echo -e "   • CLAUDE.md"
echo -e "   • team/devs/${DEV_ID}.yaml"
echo -e "   • team/rules/code-standards.md"
echo ""

# Vérifier si Claude CLI est installé
if command -v claude &> /dev/null; then
    echo -e "${GREEN}✓ Claude CLI détecté${NC}"
    echo ""
    echo -e "${YELLOW}Lancement de Claude Code...${NC}"
    echo ""
    
    # Lancer Claude
    claude
else
    echo -e "${YELLOW}⚠️ Claude CLI non installé${NC}"
    echo ""
    echo -e "Options:"
    echo ""
    echo -e "  1. ${CYAN}Installer Claude CLI:${NC}"
    echo -e "     npm install -g @anthropic-ai/claude-cli"
    echo ""
    echo -e "  2. ${CYAN}Utiliser Claude.ai avec ce prompt:${NC}"
    echo ""
    echo -e "${BLUE}────────────────────────────────────────────────────────────────${NC}"
    
    # Générer le prompt de contexte
    echo ""
    echo "Je travaille sur le projet Sport Equipment SaaS."
    echo ""
    
    if [ -n "$TICKET_ID" ] && [ "$TICKET_ID" != "null" ]; then
        TICKET_TITLE=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "title:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "$TICKET_ID")
        echo "## Ticket en cours: ${TICKET_ID} - ${TICKET_TITLE}"
        echo ""
    fi
    
    echo "## Règles"
    echo "- Monolithe modulaire Clean Architecture"
    echo "- Entités héritent de TenantAwareEntity"
    echo "- Pas d'import entre modules (utiliser Events)"
    echo ""
    echo "## Stack"
    echo "- Java 17 + Spring Boot 3.3 + PostgreSQL"
    echo "- Angular 17+ + TailwindCSS"
    echo ""
    echo "Voir CLAUDE.md pour le contexte complet."
    echo ""
    
    echo -e "${BLUE}────────────────────────────────────────────────────────────────${NC}"
    echo ""
    echo -e "  3. ${CYAN}Voir un prompt spécifique:${NC}"
    echo -e "     ${GREEN}make prompt TYPE=entity${NC}"
    echo -e "     ${GREEN}make prompt TYPE=service${NC}"
    echo -e "     ${GREEN}make prompt TYPE=api${NC}"
    echo ""
fi
