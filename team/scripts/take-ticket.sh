#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: take-ticket.sh
# Usage: ./team/scripts/take-ticket.sh <TICKET_ID> <DEV_ID> [OPENPROJECT_ID]
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
TICKET_ID=$1
DEV_ID=${2:-$(whoami)}
OPENPROJECT_ID=${3:-0}

# Fichiers
DEV_FILE="team/devs/${DEV_ID}.yaml"
CURRENT_FILE="team/tickets/current.yaml"
CONFIG_FILE="$HOME/.sport-saas/config"

# ═══════════════════════════════════════════════════════════════════
# Pré-vérification des accès
# ═══════════════════════════════════════════════════════════════════

if ! ./team/scripts/check-access.sh --quiet 2>/dev/null; then
    echo ""
    echo -e "${RED}❌ Accès non configurés !${NC}"
    echo ""
    echo -e "Lance d'abord: ${YELLOW}make access${NC}"
    echo ""
    exit 1
fi

# ═══════════════════════════════════════════════════════════════════
# Validation
# ═══════════════════════════════════════════════════════════════════

if [ -z "$TICKET_ID" ]; then
    echo -e "${RED}❌ Usage: make ticket TICKET=E1-002 ID=42${NC}"
    echo ""
    echo -e "Tickets disponibles:"
    ./team/scripts/list-tickets.sh 2>/dev/null || echo "Voir team/tickets/current.yaml"
    exit 1
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   🎫 PRISE DU TICKET: ${TICKET_ID}${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# ═══════════════════════════════════════════════════════════════════
# 1. Synchronisation
# ═══════════════════════════════════════════════════════════════════

echo -e "${CYAN}[1/5] Synchronisation avec develop...${NC}"

# Sauvegarder les changements locaux si nécessaire
if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
    echo -e "  ${YELLOW}→ Stash des changements locaux${NC}"
    git stash push -m "auto-stash before ticket $TICKET_ID"
fi

git checkout develop 2>/dev/null || git checkout -b develop
git pull origin develop 2>/dev/null || true
echo -e "  ${GREEN}✓ Synchronisé${NC}"

# ═══════════════════════════════════════════════════════════════════
# 2. Vérification du développeur
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[2/5] Vérification du développeur ${DEV_ID}...${NC}"

# Créer le fichier dev si n'existe pas
if [ ! -f "$DEV_FILE" ]; then
    echo -e "  ${YELLOW}→ Création du profil développeur${NC}"
    mkdir -p team/devs
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

history: []
EOF
fi

# Vérifier si le dev a déjà un ticket
CURRENT_TICKET=$(grep -A 10 "^current_ticket:" "$DEV_FILE" 2>/dev/null | grep "ticket_id:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")

if [ -n "$CURRENT_TICKET" ] && [ "$CURRENT_TICKET" != "null" ]; then
    echo -e "  ${RED}❌ Tu as déjà un ticket en cours: ${CURRENT_TICKET}${NC}"
    echo ""
    echo -e "  Termine-le d'abord avec: ${YELLOW}make done${NC}"
    exit 1
fi
echo -e "  ${GREEN}✓ Développeur prêt${NC}"

# ═══════════════════════════════════════════════════════════════════
# 3. Vérification du ticket
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[3/5] Vérification du ticket ${TICKET_ID}...${NC}"

# Chercher le ticket dans current.yaml
TICKET_INFO=""
TICKET_TITLE=""

if [ -f "$CURRENT_FILE" ]; then
    # Chercher dans ready:
    if grep -q "ticket_id: \"$TICKET_ID\"" "$CURRENT_FILE"; then
        TICKET_TITLE=$(grep -A 3 "ticket_id: \"$TICKET_ID\"" "$CURRENT_FILE" | grep "title:" | head -1 | sed 's/.*title: "\([^"]*\)".*/\1/')
        echo -e "  ${GREEN}✓ Ticket trouvé: ${TICKET_TITLE}${NC}"
    else
        echo -e "  ${YELLOW}⚠️ Ticket non trouvé dans current.yaml${NC}"
        echo -e "  ${GRAY}  Continuation avec le ticket personnalisé...${NC}"
    fi
fi

# Vérifier si le ticket n'est pas déjà pris
TAKEN_BY=$(grep -B 5 "ticket_id: \"$TICKET_ID\"" "$CURRENT_FILE" 2>/dev/null | grep -A 10 "in_progress:" | grep "dev:" | head -1 | grep -o '"[^"]*"' | tr -d '"' || echo "")

if [ -n "$TAKEN_BY" ] && [ "$TAKEN_BY" != "$DEV_ID" ]; then
    echo -e "  ${RED}❌ Ticket déjà pris par: ${TAKEN_BY}${NC}"
    exit 1
fi

echo -e "  ${GREEN}✓ Ticket disponible${NC}"

# ═══════════════════════════════════════════════════════════════════
# 4. Création de la branche
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[4/5] Création de la branche...${NC}"

# Construire le nom de la branche
BRANCH_SUFFIX=$(echo "$TICKET_ID" | tr '[:upper:]' '[:lower:]' | tr ' ' '-')
BRANCH_NAME="feature/${OPENPROJECT_ID}-${BRANCH_SUFFIX}"

# Vérifier si la branche existe
if git show-ref --verify --quiet "refs/heads/$BRANCH_NAME" 2>/dev/null; then
    echo -e "  ${YELLOW}→ Branche existante, checkout${NC}"
    git checkout "$BRANCH_NAME"
else
    git checkout -b "$BRANCH_NAME"
fi

echo -e "  ${GREEN}✓ Branche: ${BRANCH_NAME}${NC}"

# ═══════════════════════════════════════════════════════════════════
# 5. Mise à jour des fichiers de suivi
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[5/5] Mise à jour du suivi...${NC}"

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S")

# Mettre à jour le fichier du dev avec le ticket en cours
# (Utilisation de sed pour modifier le YAML - simple mais fonctionnel)
cat > "$DEV_FILE" << EOF
dev:
  id: "${DEV_ID}"
  name: "${DEV_ID}"
  email: "${DEV_ID}@example.com"
  slack: "@${DEV_ID}"
  role: "Développeur"
  joined_at: "$(date +%Y-%m-%d)"

current_ticket:
  ticket_id: "${TICKET_ID}"
  openproject_id: ${OPENPROJECT_ID}
  title: "${TICKET_TITLE:-$TICKET_ID}"
  branch: "${BRANCH_NAME}"
  started_at: "${TIMESTAMP}"

claude_preferences:
  explanation_level: "normal"
  auto_generate_tests: true

history: []
EOF

echo -e "  ${GREEN}✓${NC} Fichiers locaux mis à jour"

# Mettre à jour OpenProject si l'ID est fourni
if [ "$OPENPROJECT_ID" != "0" ] && [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
    if [ -n "$OPENPROJECT_TOKEN" ]; then
        echo -e "  ${GRAY}→ Mise à jour OpenProject...${NC}"
        
        # Assigner le ticket au dev
        ./team/scripts/openproject-api.sh assign "$OPENPROJECT_ID" 2>/dev/null && \
            echo -e "  ${GREEN}✓${NC} Ticket assigné à toi" || \
            echo -e "  ${YELLOW}⚠️${NC} Assignation échouée"
        
        # Passer en "In Progress"
        ./team/scripts/openproject-api.sh start "$OPENPROJECT_ID" 2>/dev/null && \
            echo -e "  ${GREEN}✓${NC} OpenProject → In Progress" || \
            echo -e "  ${YELLOW}⚠️${NC} Status non mis à jour"
    fi
fi

# Commit des fichiers de suivi
git add team/devs/ team/tickets/ 2>/dev/null || true
git commit -m "chore: ${DEV_ID} starts ticket ${TICKET_ID}" --allow-empty 2>/dev/null || true

# ═══════════════════════════════════════════════════════════════════
# Résumé
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ TICKET ${TICKET_ID} ASSIGNÉ À ${DEV_ID}${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "🌿 ${CYAN}Branche:${NC} ${BRANCH_NAME}"
echo ""
echo -e "${YELLOW}📋 PROCHAINE ÉTAPE:${NC}"
echo ""
echo -e "   ${GREEN}make work${NC}"
echo ""
echo -e "   ${GRAY}Cette commande va:${NC}"
echo -e "   ${GRAY}1. Ouvrir Claude Code pour coder${NC}"
echo -e "   ${GRAY}2. Te demander un message de commit${NC}"
echo -e "   ${GRAY}3. Push + Créer la PR automatiquement${NC}"
echo -e "   ${GRAY}4. Mettre à jour OpenProject → In Review${NC}"
echo ""
echo -e "   ${GRAY}Ou étape par étape:${NC}"
echo -e "   ${GRAY}make claude → make commit MSG=\"...\" → make done${NC}"
echo ""
