#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: setup-access.sh
# Description: Configure tous les accès nécessaires (Git, SSH, GitHub CLI, OpenProject)
# Usage: ./team/scripts/setup-access.sh
# ═══════════════════════════════════════════════════════════════════

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

CONFIG_DIR="$HOME/.sport-saas"
CONFIG_FILE="$CONFIG_DIR/config"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}            🔐 CONFIGURATION DES ACCÈS${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Créer le dossier de config
mkdir -p "$CONFIG_DIR"
touch "$CONFIG_FILE"
chmod 600 "$CONFIG_FILE"

# ═══════════════════════════════════════════════════════════════════
# 1. GIT USER
# ═══════════════════════════════════════════════════════════════════

echo -e "${CYAN}[1/4] Configuration Git${NC}"
echo ""

GIT_NAME=$(git config --global user.name 2>/dev/null || echo "")
GIT_EMAIL=$(git config --global user.email 2>/dev/null || echo "")

if [ -z "$GIT_NAME" ]; then
    read -p "  Nom complet (ex: Alice Dupont): " GIT_NAME
    git config --global user.name "$GIT_NAME"
fi

if [ -z "$GIT_EMAIL" ]; then
    read -p "  Email: " GIT_EMAIL
    git config --global user.email "$GIT_EMAIL"
fi

echo -e "  ${GREEN}✓${NC} Git: $GIT_NAME <$GIT_EMAIL>"

# ═══════════════════════════════════════════════════════════════════
# 2. GITHUB SSH
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[2/4] Configuration SSH GitHub${NC}"
echo ""

# Vérifier si SSH est déjà configuré
SSH_OUTPUT=$(ssh -T git@github.com 2>&1 || true)
if echo "$SSH_OUTPUT" | grep -q "Hi "; then
    GH_USER=$(echo "$SSH_OUTPUT" | grep -o "Hi [^!]*" | cut -d' ' -f2)
    echo -e "  ${GREEN}✓${NC} SSH déjà configuré pour: $GH_USER"
else
    echo -e "  ${YELLOW}SSH non configuré pour GitHub${NC}"
    echo ""
    
    # Vérifier si une clé existe
    if [ -f "$HOME/.ssh/id_ed25519" ] || [ -f "$HOME/.ssh/id_rsa" ]; then
        echo -e "  ${GRAY}Une clé SSH existe déjà.${NC}"
        
        if [ -f "$HOME/.ssh/id_ed25519.pub" ]; then
            KEY_FILE="$HOME/.ssh/id_ed25519.pub"
        else
            KEY_FILE="$HOME/.ssh/id_rsa.pub"
        fi
        
        echo ""
        echo -e "  ${YELLOW}Copie cette clé sur GitHub:${NC}"
        echo -e "  ${BLUE}https://github.com/settings/ssh/new${NC}"
        echo ""
        echo -e "  ${GRAY}───────────────────────────────────────${NC}"
        cat "$KEY_FILE"
        echo -e "  ${GRAY}───────────────────────────────────────${NC}"
        echo ""
    else
        echo -e "  ${YELLOW}Génération d'une nouvelle clé SSH...${NC}"
        echo ""
        
        read -p "  Email pour la clé (Entrée = $GIT_EMAIL): " SSH_EMAIL
        SSH_EMAIL=${SSH_EMAIL:-$GIT_EMAIL}
        
        ssh-keygen -t ed25519 -C "$SSH_EMAIL" -f "$HOME/.ssh/id_ed25519" -N ""
        
        echo ""
        echo -e "  ${GREEN}✓${NC} Clé générée: ~/.ssh/id_ed25519"
        echo ""
        echo -e "  ${YELLOW}Copie cette clé sur GitHub:${NC}"
        echo -e "  ${BLUE}https://github.com/settings/ssh/new${NC}"
        echo ""
        echo -e "  ${GRAY}───────────────────────────────────────${NC}"
        cat "$HOME/.ssh/id_ed25519.pub"
        echo -e "  ${GRAY}───────────────────────────────────────${NC}"
        echo ""
    fi
    
    # Démarrer l'agent SSH
    eval "$(ssh-agent -s)" > /dev/null 2>&1
    ssh-add ~/.ssh/id_ed25519 2>/dev/null || ssh-add ~/.ssh/id_rsa 2>/dev/null || true
    
    read -p "  Appuie sur Entrée après avoir ajouté la clé sur GitHub..."
    
    # Tester
    echo ""
    echo -e "  ${GRAY}Test de la connexion SSH...${NC}"
    SSH_TEST=$(ssh -T git@github.com 2>&1 || true)
    
    if echo "$SSH_TEST" | grep -q "Hi "; then
        echo -e "  ${GREEN}✓${NC} SSH GitHub configuré!"
    else
        echo -e "  ${RED}✗${NC} SSH GitHub non fonctionnel"
        echo -e "  ${GRAY}Erreur: $SSH_TEST${NC}"
        echo ""
        echo -e "  ${YELLOW}Réessaie plus tard avec: make access${NC}"
    fi
fi

# ═══════════════════════════════════════════════════════════════════
# 3. GITHUB CLI
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[3/4] Configuration GitHub CLI${NC}"
echo ""

if command -v gh &> /dev/null; then
    if gh auth status &> /dev/null 2>&1; then
        GH_USER=$(gh api user --jq '.login' 2>/dev/null || echo "OK")
        echo -e "  ${GREEN}✓${NC} GitHub CLI connecté: $GH_USER"
    else
        echo -e "  ${YELLOW}GitHub CLI non authentifié${NC}"
        echo ""
        read -p "  Lancer l'authentification? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            gh auth login
        fi
    fi
else
    echo -e "  ${RED}✗${NC} GitHub CLI non installé"
    echo ""
    echo -e "  ${YELLOW}Installe-le:${NC}"
    echo -e "    macOS:   ${CYAN}brew install gh${NC}"
    echo -e "    Ubuntu:  ${CYAN}sudo apt install gh${NC}"
    echo -e "    Windows: ${CYAN}winget install GitHub.cli${NC}"
    echo ""
    read -p "  Appuie sur Entrée après l'installation..."
    
    if command -v gh &> /dev/null; then
        echo ""
        gh auth login
    fi
fi

# ═══════════════════════════════════════════════════════════════════
# 4. OPENPROJECT
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${CYAN}[4/4] Configuration OpenProject${NC}"
echo ""

OPENPROJECT_URL="https://aam.openproject.com"

# Charger le token existant
source "$CONFIG_FILE" 2>/dev/null || true

if [ -n "$OPENPROJECT_TOKEN" ]; then
    # Tester le token
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -u "apikey:$OPENPROJECT_TOKEN" "${OPENPROJECT_URL}/api/v3/users/me" 2>/dev/null || echo "000")
    
    if [ "$RESPONSE" = "200" ]; then
        echo -e "  ${GREEN}✓${NC} Token OpenProject valide"
        
        read -p "  Reconfigurer? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            SKIP_OP=true
        fi
    else
        echo -e "  ${YELLOW}Token existant invalide${NC}"
    fi
fi

if [ "$SKIP_OP" != "true" ]; then
    echo -e "  ${YELLOW}Pour obtenir ton token:${NC}"
    echo -e "  1. Va sur: ${BLUE}${OPENPROJECT_URL}/my/access_token${NC}"
    echo -e "  2. Clique sur 'Generate' ou copie ton token"
    echo ""
    
    read -p "  Token API OpenProject: " OP_TOKEN
    
    if [ -n "$OP_TOKEN" ]; then
        # Supprimer l'ancien token
        grep -v "OPENPROJECT" "$CONFIG_FILE" > "$CONFIG_FILE.tmp" 2>/dev/null || touch "$CONFIG_FILE.tmp"
        mv "$CONFIG_FILE.tmp" "$CONFIG_FILE"
        
        echo "OPENPROJECT_TOKEN=$OP_TOKEN" >> "$CONFIG_FILE"
        echo "OPENPROJECT_URL=$OPENPROJECT_URL" >> "$CONFIG_FILE"
        
        # Tester
        RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -u "apikey:$OP_TOKEN" "${OPENPROJECT_URL}/api/v3/users/me" 2>/dev/null || echo "000")
        
        if [ "$RESPONSE" = "200" ]; then
            echo -e "  ${GREEN}✓${NC} Token OpenProject valide"
        else
            echo -e "  ${RED}✗${NC} Token invalide (code: $RESPONSE)"
        fi
    fi
fi

chmod 600 "$CONFIG_FILE"

# ═══════════════════════════════════════════════════════════════════
# VÉRIFICATION FINALE
# ═══════════════════════════════════════════════════════════════════

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    VÉRIFICATION FINALE${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

./team/scripts/check-access.sh

echo ""
