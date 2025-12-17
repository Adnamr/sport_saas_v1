#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: check-access.sh
# Description: Vérifie TOUS les accès requis et guide l'utilisateur
# Usage: ./team/scripts/check-access.sh [--quiet]
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

QUIET=${1:-""}
CONFIG_FILE="$HOME/.sport-saas/config"

ERRORS=0
NEXT_STEP=""

# Charger la config si elle existe
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
fi

# ═══════════════════════════════════════════════════════════════════
# 1. Vérifier Git user
# ═══════════════════════════════════════════════════════════════════

GIT_NAME=$(git config user.name 2>/dev/null || echo "")
GIT_EMAIL=$(git config user.email 2>/dev/null || echo "")

if [ -z "$GIT_NAME" ] || [ -z "$GIT_EMAIL" ]; then
    if [ "$QUIET" != "--quiet" ]; then
        echo -e "${RED}✗ Git user non configuré${NC}"
    fi
    ERRORS=$((ERRORS + 1))
    if [ -z "$NEXT_STEP" ]; then
        NEXT_STEP="git"
    fi
else
    if [ "$QUIET" != "--quiet" ]; then
        echo -e "${GREEN}✓ Git:${NC} $GIT_NAME <$GIT_EMAIL>"
    fi
fi

# ═══════════════════════════════════════════════════════════════════
# 2. Vérifier GitHub SSH
# ═══════════════════════════════════════════════════════════════════

GITHUB_SSH_OK=false

# Test SSH connection to GitHub
SSH_OUTPUT=$(ssh -T git@github.com 2>&1)
if echo "$SSH_OUTPUT" | grep -q "successfully authenticated"; then
    GITHUB_SSH_OK=true
elif echo "$SSH_OUTPUT" | grep -q "Hi "; then
    GITHUB_SSH_OK=true
fi

if [ "$GITHUB_SSH_OK" = true ]; then
    if [ "$QUIET" != "--quiet" ]; then
        GH_USER=$(echo "$SSH_OUTPUT" | grep -o "Hi [^!]*" | cut -d' ' -f2 || echo "OK")
        echo -e "${GREEN}✓ GitHub SSH:${NC} Connecté ($GH_USER)"
    fi
else
    if [ "$QUIET" != "--quiet" ]; then
        echo -e "${RED}✗ GitHub SSH non configuré${NC}"
    fi
    ERRORS=$((ERRORS + 1))
    if [ -z "$NEXT_STEP" ]; then
        NEXT_STEP="github_ssh"
    fi
fi

# ═══════════════════════════════════════════════════════════════════
# 3. Vérifier GitHub CLI (pour créer les PR)
# ═══════════════════════════════════════════════════════════════════

GH_CLI_OK=false

if command -v gh &> /dev/null; then
    if gh auth status &> /dev/null 2>&1; then
        GH_CLI_OK=true
        if [ "$QUIET" != "--quiet" ]; then
            GH_USER=$(gh api user --jq '.login' 2>/dev/null || echo "OK")
            echo -e "${GREEN}✓ GitHub CLI:${NC} Connecté ($GH_USER)"
        fi
    else
        if [ "$QUIET" != "--quiet" ]; then
            echo -e "${RED}✗ GitHub CLI non authentifié${NC}"
        fi
        ERRORS=$((ERRORS + 1))
        if [ -z "$NEXT_STEP" ]; then
            NEXT_STEP="github_cli_auth"
        fi
    fi
else
    if [ "$QUIET" != "--quiet" ]; then
        echo -e "${RED}✗ GitHub CLI (gh) non installé${NC}"
    fi
    ERRORS=$((ERRORS + 1))
    if [ -z "$NEXT_STEP" ]; then
        NEXT_STEP="github_cli_install"
    fi
fi

# ═══════════════════════════════════════════════════════════════════
# 4. Vérifier OpenProject
# ═══════════════════════════════════════════════════════════════════

if [ -n "$OPENPROJECT_TOKEN" ]; then
    # Tester le token
    OPENPROJECT_URL=${OPENPROJECT_URL:-"https://aam.openproject.com"}
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -u "apikey:$OPENPROJECT_TOKEN" "${OPENPROJECT_URL}/api/v3/users/me" 2>/dev/null || echo "000")
    
    if [ "$RESPONSE" = "200" ]; then
        if [ "$QUIET" != "--quiet" ]; then
            echo -e "${GREEN}✓ OpenProject:${NC} Connecté"
        fi
    else
        if [ "$QUIET" != "--quiet" ]; then
            echo -e "${RED}✗ OpenProject token invalide${NC}"
        fi
        ERRORS=$((ERRORS + 1))
        if [ -z "$NEXT_STEP" ]; then
            NEXT_STEP="openproject_token"
        fi
    fi
else
    if [ "$QUIET" != "--quiet" ]; then
        echo -e "${RED}✗ OpenProject non configuré${NC}"
    fi
    ERRORS=$((ERRORS + 1))
    if [ -z "$NEXT_STEP" ]; then
        NEXT_STEP="openproject"
    fi
fi

# ═══════════════════════════════════════════════════════════════════
# Résultat et guidance
# ═══════════════════════════════════════════════════════════════════

if [ $ERRORS -gt 0 ]; then
    if [ "$QUIET" != "--quiet" ]; then
        echo ""
        echo -e "${RED}═══════════════════════════════════════════════════════════════${NC}"
        echo -e "${RED}   ❌ ACCÈS INCOMPLETS - $ERRORS problème(s) détecté(s)${NC}"
        echo -e "${RED}═══════════════════════════════════════════════════════════════${NC}"
        echo ""
        
        # Proposer l'étape suivante
        case "$NEXT_STEP" in
            "git")
                echo -e "${YELLOW}📋 PROCHAINE ÉTAPE: Configurer Git${NC}"
                echo ""
                echo -e "   Exécute ces commandes:"
                echo -e "   ${CYAN}git config --global user.name \"Ton Nom\"${NC}"
                echo -e "   ${CYAN}git config --global user.email \"ton@email.com\"${NC}"
                ;;
            "github_ssh")
                echo -e "${YELLOW}📋 PROCHAINE ÉTAPE: Configurer SSH pour GitHub${NC}"
                echo ""
                echo -e "   1. Génère une clé SSH (si pas déjà fait):"
                echo -e "      ${CYAN}ssh-keygen -t ed25519 -C \"ton@email.com\"${NC}"
                echo ""
                echo -e "   2. Ajoute la clé à l'agent SSH:"
                echo -e "      ${CYAN}eval \"\$(ssh-agent -s)\"${NC}"
                echo -e "      ${CYAN}ssh-add ~/.ssh/id_ed25519${NC}"
                echo ""
                echo -e "   3. Copie la clé publique:"
                echo -e "      ${CYAN}cat ~/.ssh/id_ed25519.pub${NC}"
                echo ""
                echo -e "   4. Ajoute-la sur GitHub:"
                echo -e "      ${BLUE}https://github.com/settings/ssh/new${NC}"
                echo ""
                echo -e "   5. Teste la connexion:"
                echo -e "      ${CYAN}ssh -T git@github.com${NC}"
                ;;
            "github_cli_install")
                echo -e "${YELLOW}📋 PROCHAINE ÉTAPE: Installer GitHub CLI${NC}"
                echo ""
                echo -e "   macOS:   ${CYAN}brew install gh${NC}"
                echo -e "   Ubuntu:  ${CYAN}sudo apt install gh${NC}"
                echo -e "   Windows: ${CYAN}winget install GitHub.cli${NC}"
                echo ""
                echo -e "   Puis relance: ${GREEN}make access${NC}"
                ;;
            "github_cli_auth")
                echo -e "${YELLOW}📋 PROCHAINE ÉTAPE: Authentifier GitHub CLI${NC}"
                echo ""
                echo -e "   Exécute:"
                echo -e "   ${CYAN}gh auth login${NC}"
                echo ""
                echo -e "   Choisis: GitHub.com → SSH → Oui"
                ;;
            "openproject"|"openproject_token")
                echo -e "${YELLOW}📋 PROCHAINE ÉTAPE: Configurer OpenProject${NC}"
                echo ""
                echo -e "   1. Va sur: ${BLUE}https://aam.openproject.com/my/access_token${NC}"
                echo -e "   2. Clique sur 'Generate' ou copie ton token existant"
                echo -e "   3. Relance: ${GREEN}make access${NC}"
                ;;
        esac
        
        echo ""
        echo -e "${GRAY}Ou lance la configuration complète: ${GREEN}make access${NC}"
        echo ""
    fi
    exit 1
else
    if [ "$QUIET" != "--quiet" ]; then
        echo ""
        echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
        echo -e "${GREEN}   ✅ TOUS LES ACCÈS SONT CONFIGURÉS${NC}"
        echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
        echo ""
        echo -e "${YELLOW}📋 PROCHAINE ÉTAPE:${NC} ${GREEN}make dev DEV=ton_nom${NC}"
        echo ""
    fi
    exit 0
fi
