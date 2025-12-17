#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: list-features.sh
# Liste les features disponibles et leur statut
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
WHITE='\033[1;37m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"

PROJECT_NAME=$(grep "name:" "$PROJECT_DIR/project.yaml" 2>/dev/null | head -1 | sed 's/.*: "\(.*\)"/\1/' | sed 's/.*: //')

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}        📋 FEATURES - ${PROJECT_NAME}${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Vérifier si le dossier features existe
if [ ! -d "$PROJECT_DIR/team/features" ]; then
    echo -e "${YELLOW}⚠️  Aucune feature définie.${NC}"
    echo ""
    echo -e "Créer une feature:"
    echo -e "  ${GREEN}cp team/features/_template.yaml team/features/ma-feature.yaml${NC}"
    echo ""
    exit 0
fi

# Compter les features
FEATURE_COUNT=$(ls -1 "$PROJECT_DIR/team/features/"*.yaml 2>/dev/null | grep -v "_template" | wc -l)

if [ "$FEATURE_COUNT" -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Aucune feature définie.${NC}"
    echo ""
    echo -e "Créer une feature:"
    echo -e "  ${GREEN}cp team/features/_template.yaml team/features/ma-feature.yaml${NC}"
    echo ""
    exit 0
fi

# Récupérer les features en cours par dev
declare -A IN_PROGRESS
for dev_file in "$PROJECT_DIR/team/devs/"*.yaml; do
    [ -f "$dev_file" ] || continue
    [[ "$(basename "$dev_file")" == "_template.yaml" ]] && continue
    
    DEV_NAME=$(basename "$dev_file" .yaml)
    CURRENT_FEATURE=$(grep "feature_name:" "$dev_file" 2>/dev/null | sed 's/.*: "\(.*\)"/\1/' || echo "")
    
    if [ -n "$CURRENT_FEATURE" ]; then
        IN_PROGRESS["$CURRENT_FEATURE"]="$DEV_NAME"
    fi
done

# Afficher les features
echo -e "${CYAN}▸ FEATURES DISPONIBLES ($FEATURE_COUNT)${NC}"
echo ""

for feature_file in "$PROJECT_DIR/team/features/"*.yaml; do
    [ -f "$feature_file" ] || continue
    [[ "$(basename "$feature_file")" == "_template.yaml" ]] && continue
    
    FNAME=$(basename "$feature_file" .yaml)
    FID=$(grep "^  id:" "$feature_file" 2>/dev/null | head -1 | sed 's/.*: "\([^"]*\)".*/\1/' || echo "?")
    FTITLE=$(grep "^  name:" "$feature_file" 2>/dev/null | head -1 | sed 's/.*: "\([^"]*\)".*/\1/' || echo "$FNAME")
    FPRIORITY=$(grep "^  priority:" "$feature_file" 2>/dev/null | head -1 | sed 's/.*: "\([^"]*\)".*/\1/' || echo "medium")
    FEST=$(grep "^  estimated_days:" "$feature_file" 2>/dev/null | head -1 | awk '{print $2}' || echo "?")
    
    # Couleur selon priorité
    PRIO_COLOR="${GRAY}"
    case "$FPRIORITY" in
        "high") PRIO_COLOR="${RED}" ;;
        "medium") PRIO_COLOR="${YELLOW}" ;;
        "low") PRIO_COLOR="${GRAY}" ;;
    esac
    
    # Status
    STATUS_ICON="○"
    STATUS_TEXT=""
    
    if [ -n "${IN_PROGRESS[$FNAME]}" ]; then
        STATUS_ICON="●"
        STATUS_TEXT="${CYAN}(${IN_PROGRESS[$FNAME]})${NC}"
    fi
    
    # Vérifier si branche existe
    if git show-ref --verify --quiet "refs/heads/feature/$FNAME" 2>/dev/null; then
        if [ -z "$STATUS_TEXT" ]; then
            STATUS_ICON="◐"
            STATUS_TEXT="${GRAY}(branche existe)${NC}"
        fi
    fi
    
    printf "  ${GREEN}${STATUS_ICON}${NC} %-12s ${PRIO_COLOR}[%-6s]${NC} %-35s ${GRAY}~${FEST}j${NC} ${STATUS_TEXT}\n" \
        "$FNAME" "$FPRIORITY" "$FTITLE"
done

echo ""
echo -e "${GRAY}───────────────────────────────────────────────────────────────${NC}"
echo ""
echo -e "${WHITE}Légende:${NC}"
echo -e "  ${GREEN}○${NC} Disponible    ${GREEN}●${NC} En cours    ${GREEN}◐${NC} Commencée"
echo ""
echo -e "${WHITE}Commandes:${NC}"
echo -e "  ${GREEN}make feature FEATURE=xxx${NC}   Démarrer une feature"
echo -e "  ${GREEN}make feature-done${NC}          Terminer la feature en cours"
echo ""
