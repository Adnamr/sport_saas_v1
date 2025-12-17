#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Script: openproject-api.sh
# Description: Interagit avec l'API OpenProject
# Usage: ./team/scripts/openproject-api.sh <action> <work_package_id> [status]
# Actions: status, start, review, close, comment
# ═══════════════════════════════════════════════════════════════════

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

ACTION=$1
WP_ID=$2
EXTRA=$3

CONFIG_FILE="$HOME/.sport-saas/config"

# Charger la config
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
fi

# Vérifier le token
if [ -z "$OPENPROJECT_TOKEN" ]; then
    echo -e "${RED}❌ Token OpenProject non configuré${NC}"
    echo -e "   Lance: ${YELLOW}make access${NC}"
    exit 1
fi

OPENPROJECT_URL=${OPENPROJECT_URL:-"https://aam.openproject.com"}
API_URL="${OPENPROJECT_URL}/api/v3"

# ═══════════════════════════════════════════════════════════════════
# Fonctions
# ═══════════════════════════════════════════════════════════════════

api_get() {
    curl -s -u "apikey:$OPENPROJECT_TOKEN" "$API_URL$1"
}

api_patch() {
    curl -s -X PATCH \
        -u "apikey:$OPENPROJECT_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$2" \
        "$API_URL$1"
}

api_post() {
    curl -s -X POST \
        -u "apikey:$OPENPROJECT_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$2" \
        "$API_URL$1"
}

get_status_id() {
    case "$1" in
        "new"|"NEW")           echo "1" ;;
        "in_progress"|"IN_PROGRESS"|"start") echo "7" ;;
        "in_review"|"IN_REVIEW"|"review")    echo "8" ;;
        "closed"|"CLOSED"|"close"|"done")    echo "12" ;;
        *)                     echo "$1" ;;
    esac
}

# ═══════════════════════════════════════════════════════════════════
# Actions
# ═══════════════════════════════════════════════════════════════════

case "$ACTION" in
    "status"|"get")
        if [ -z "$WP_ID" ]; then
            echo -e "${RED}Usage: $0 status <work_package_id>${NC}"
            exit 1
        fi
        
        RESPONSE=$(api_get "/work_packages/$WP_ID")
        
        SUBJECT=$(echo "$RESPONSE" | grep -o '"subject":"[^"]*"' | head -1 | cut -d'"' -f4)
        STATUS=$(echo "$RESPONSE" | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4)
        
        echo -e "${CYAN}Ticket #${WP_ID}:${NC} $SUBJECT"
        echo -e "${CYAN}Status:${NC} $STATUS"
        ;;
        
    "start"|"in_progress")
        if [ -z "$WP_ID" ]; then
            echo -e "${RED}Usage: $0 start <work_package_id>${NC}"
            exit 1
        fi
        
        STATUS_ID=$(get_status_id "in_progress")
        
        RESPONSE=$(api_patch "/work_packages/$WP_ID" "{\"_links\":{\"status\":{\"href\":\"/api/v3/statuses/$STATUS_ID\"}}}")
        
        if echo "$RESPONSE" | grep -q '"_type":"WorkPackage"'; then
            echo -e "${GREEN}✓ Ticket #${WP_ID} → In Progress${NC}"
        else
            echo -e "${RED}❌ Erreur lors de la mise à jour${NC}"
            echo "$RESPONSE" | head -5
        fi
        ;;
        
    "review"|"in_review")
        if [ -z "$WP_ID" ]; then
            echo -e "${RED}Usage: $0 review <work_package_id>${NC}"
            exit 1
        fi
        
        STATUS_ID=$(get_status_id "in_review")
        
        RESPONSE=$(api_patch "/work_packages/$WP_ID" "{\"_links\":{\"status\":{\"href\":\"/api/v3/statuses/$STATUS_ID\"}}}")
        
        if echo "$RESPONSE" | grep -q '"_type":"WorkPackage"'; then
            echo -e "${GREEN}✓ Ticket #${WP_ID} → In Review${NC}"
        else
            echo -e "${RED}❌ Erreur lors de la mise à jour${NC}"
        fi
        ;;
        
    "close"|"done"|"closed")
        if [ -z "$WP_ID" ]; then
            echo -e "${RED}Usage: $0 close <work_package_id>${NC}"
            exit 1
        fi
        
        STATUS_ID=$(get_status_id "closed")
        
        RESPONSE=$(api_patch "/work_packages/$WP_ID" "{\"_links\":{\"status\":{\"href\":\"/api/v3/statuses/$STATUS_ID\"}}}")
        
        if echo "$RESPONSE" | grep -q '"_type":"WorkPackage"'; then
            echo -e "${GREEN}✓ Ticket #${WP_ID} → Closed${NC}"
        else
            echo -e "${RED}❌ Erreur lors de la mise à jour${NC}"
        fi
        ;;
        
    "comment")
        if [ -z "$WP_ID" ] || [ -z "$EXTRA" ]; then
            echo -e "${RED}Usage: $0 comment <work_package_id> \"commentaire\"${NC}"
            exit 1
        fi
        
        RESPONSE=$(api_post "/work_packages/$WP_ID/activities" "{\"comment\":{\"raw\":\"$EXTRA\"}}")
        
        if echo "$RESPONSE" | grep -q '"_type":"Activity"'; then
            echo -e "${GREEN}✓ Commentaire ajouté au ticket #${WP_ID}${NC}"
        else
            echo -e "${RED}❌ Erreur lors de l'ajout du commentaire${NC}"
        fi
        ;;
    
    "assign")
        if [ -z "$WP_ID" ]; then
            echo -e "${RED}Usage: $0 assign <work_package_id> [user_id]${NC}"
            exit 1
        fi
        
        # Si pas de user_id fourni, utiliser "me"
        if [ -z "$EXTRA" ]; then
            # Récupérer l'ID de l'utilisateur courant
            USER_RESPONSE=$(api_get "/users/me")
            USER_ID=$(echo "$USER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        else
            USER_ID="$EXTRA"
        fi
        
        if [ -z "$USER_ID" ]; then
            echo -e "${RED}❌ Impossible de récupérer l'ID utilisateur${NC}"
            exit 1
        fi
        
        RESPONSE=$(api_patch "/work_packages/$WP_ID" "{\"_links\":{\"assignee\":{\"href\":\"/api/v3/users/$USER_ID\"}}}")
        
        if echo "$RESPONSE" | grep -q '"_type":"WorkPackage"'; then
            echo -e "${GREEN}✓ Ticket #${WP_ID} assigné${NC}"
        else
            echo -e "${RED}❌ Erreur lors de l'assignation${NC}"
            echo "$RESPONSE" | head -3
        fi
        ;;
        
    "list"|"my")
        # Lister mes tickets assignés
        RESPONSE=$(api_get "/work_packages?filters=[{\"assignee\":{\"operator\":\"=\",\"values\":[\"me\"]}}]")
        
        echo -e "${CYAN}Mes tickets:${NC}"
        echo "$RESPONSE" | grep -o '"subject":"[^"]*"' | while read -r line; do
            SUBJECT=$(echo "$line" | cut -d'"' -f4)
            echo -e "  • $SUBJECT"
        done
        ;;
        
    "test")
        # Tester la connexion
        RESPONSE=$(api_get "/users/me")
        
        if echo "$RESPONSE" | grep -q '"_type":"User"'; then
            NAME=$(echo "$RESPONSE" | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4)
            echo -e "${GREEN}✓ Connecté en tant que: ${NAME}${NC}"
        else
            echo -e "${RED}❌ Connexion échouée${NC}"
            exit 1
        fi
        ;;
        
    *)
        echo -e "${CYAN}Usage:${NC} $0 <action> <work_package_id> [extra]"
        echo ""
        echo -e "${CYAN}Actions:${NC}"
        echo "  status <id>        Afficher le statut d'un ticket"
        echo "  start <id>         Passer en 'In Progress'"
        echo "  review <id>        Passer en 'In Review'"
        echo "  close <id>         Passer en 'Closed'"
        echo "  comment <id> \"msg\" Ajouter un commentaire"
        echo "  list               Lister mes tickets"
        echo "  test               Tester la connexion"
        ;;
esac
