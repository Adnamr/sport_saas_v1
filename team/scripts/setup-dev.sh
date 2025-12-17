#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════
# Script: setup-dev.sh
# Description: Configure un nouveau développeur
# Usage: ./team/scripts/setup-dev.sh <dev_id>
# ═══════════════════════════════════════════════════════════════════════════

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

DEV_ID=${1:-$(whoami)}
DEV_FILE="team/devs/${DEV_ID}.yaml"

echo -e "${CYAN}Configuration du développeur: ${DEV_ID}${NC}"
echo ""

# Vérifier si le fichier existe déjà
if [ -f "$DEV_FILE" ]; then
    echo -e "${YELLOW}⚠️  Le fichier ${DEV_FILE} existe déjà.${NC}"
    read -p "Voulez-vous le recréer? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${GREEN}✓ Configuration existante conservée${NC}"
        exit 0
    fi
fi

# Demander les informations
echo -e "${CYAN}Entrez vos informations:${NC}"
echo ""

read -p "Nom complet (ex: Alice Dupont): " FULL_NAME
FULL_NAME=${FULL_NAME:-$DEV_ID}

read -p "Email: " EMAIL
EMAIL=${EMAIL:-"${DEV_ID}@example.com"}

read -p "Slack ID (ex: @alice): " SLACK_ID
SLACK_ID=${SLACK_ID:-"@${DEV_ID}"}

read -p "Rôle (Développeur/Dev Senior/Tech Lead): " ROLE
ROLE=${ROLE:-"Développeur"}

# Créer le fichier
cat > "$DEV_FILE" << EOF
# 👤 Fichier Développeur - ${DEV_ID}
# Généré le $(date +%Y-%m-%d)

dev:
  id: "${DEV_ID}"
  name: "${FULL_NAME}"
  email: "${EMAIL}"
  slack: "${SLACK_ID}"
  role: "${ROLE}"
  joined_at: "$(date +%Y-%m-%d)"
  
  access:
    github: true
    openproject: true
    slack: true
    server_dev: false

# Ticket en cours (null = aucun)
current_ticket: null

# Préférences Claude Code
claude_preferences:
  explanation_level: "normal"
  auto_generate_tests: true
  code_style:
    use_lombok: true
    use_records_for_dto: true
    max_line_length: 120

# Historique des tickets terminés
history: []
EOF

echo ""
echo -e "${GREEN}✓ Fichier développeur créé: ${DEV_FILE}${NC}"
echo ""

# Configurer git si demandé
read -p "Configurer Git avec ces informations? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    git config user.name "${FULL_NAME}"
    git config user.email "${EMAIL}"
    echo -e "${GREEN}✓ Git configuré${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ Développeur ${DEV_ID} configuré !${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}📋 PROCHAINE ÉTAPE:${NC}"
echo ""
echo -e "   1. Voir les tickets: ${GREEN}make tickets${NC}"
echo -e "   2. Prendre un ticket: ${GREEN}make ticket TICKET=E1-001 ID=1${NC}"
echo ""
