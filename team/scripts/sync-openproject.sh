#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# SYNC OPENPROJECT → YAML
# ═══════════════════════════════════════════════════════════════════
#
# Import les work packages d'OpenProject vers fichiers YAML locaux
#
# Usage:
#   ./sync-openproject.sh tickets              → Importe les Tasks/Bugs
#   ./sync-openproject.sh features             → Importe les Features/Epics
#   ./sync-openproject.sh all                  → Importe tout
#
# Options:
#   --project=ID    Filtrer par projet OpenProject
#   --status=open   Filtrer: open, closed, all (défaut: open)
#   --sprint=NAME   Filtrer par sprint/version
#
# ═══════════════════════════════════════════════════════════════════

set -e

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEAM_DIR="$(dirname "$SCRIPT_DIR")"
CONFIG_FILE="$HOME/.sport-saas/config"

# ═══════════════════════════════════════════════════════════════════
# CONFIG
# ═══════════════════════════════════════════════════════════════════

if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
fi

if [ -z "$OPENPROJECT_TOKEN" ]; then
    echo -e "${RED}❌ Token OpenProject non configuré${NC}"
    echo -e "   Lance: ${YELLOW}make access${NC}"
    exit 1
fi

OPENPROJECT_URL=${OPENPROJECT_URL:-"https://aam.openproject.com"}
API_URL="${OPENPROJECT_URL}/api/v3"

# Paramètres par défaut
TYPE="$1"
PROJECT_ID=""
STATUS_FILTER="open"
SPRINT_FILTER=""

# Parser les options
shift || true
for arg in "$@"; do
    case $arg in
        --project=*)
            PROJECT_ID="${arg#*=}"
            ;;
        --status=*)
            STATUS_FILTER="${arg#*=}"
            ;;
        --sprint=*)
            SPRINT_FILTER="${arg#*=}"
            ;;
    esac
done

# ═══════════════════════════════════════════════════════════════════
# API FUNCTIONS
# ═══════════════════════════════════════════════════════════════════

api_get() {
    curl -s -u "apikey:$OPENPROJECT_TOKEN" "$API_URL$1"
}

# Construire les filtres
build_filters() {
    local filters="["
    local first=true
    
    # Filtre par type
    if [[ "$1" == "tickets" ]]; then
        filters+="{\"type\":{\"operator\":\"=\",\"values\":[\"Task\",\"Bug\"]}}"
        first=false
    elif [[ "$1" == "features" ]]; then
        if [[ "$first" == false ]]; then filters+=","; fi
        filters+="{\"type\":{\"operator\":\"=\",\"values\":[\"Feature\",\"Epic\",\"User Story\"]}}"
        first=false
    fi
    
    # Filtre par statut
    if [[ "$STATUS_FILTER" == "open" ]]; then
        if [[ "$first" == false ]]; then filters+=","; fi
        filters+="{\"status\":{\"operator\":\"o\",\"values\":[]}}"
        first=false
    elif [[ "$STATUS_FILTER" == "closed" ]]; then
        if [[ "$first" == false ]]; then filters+=","; fi
        filters+="{\"status\":{\"operator\":\"c\",\"values\":[]}}"
        first=false
    fi
    
    # Filtre par projet
    if [[ -n "$PROJECT_ID" ]]; then
        if [[ "$first" == false ]]; then filters+=","; fi
        filters+="{\"project\":{\"operator\":\"=\",\"values\":[\"$PROJECT_ID\"]}}"
        first=false
    fi
    
    filters+="]"
    echo "$filters"
}

# ═══════════════════════════════════════════════════════════════════
# IMPORT TICKETS
# ═══════════════════════════════════════════════════════════════════

import_tickets() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}              📥 IMPORT TICKETS DEPUIS OPENPROJECT${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    
    local filters=$(build_filters "tickets")
    local encoded_filters=$(echo "$filters" | sed 's/ /%20/g' | sed 's/\[/%5B/g' | sed 's/\]/%5D/g' | sed 's/{/%7B/g' | sed 's/}/%7D/g' | sed 's/"/%22/g' | sed 's/:/%3A/g' | sed 's/,/%2C/g')
    
    echo -e "${CYAN}Récupération des tickets...${NC}"
    
    RESPONSE=$(api_get "/work_packages?pageSize=100&filters=$encoded_filters")
    
    # Vérifier si la réponse est valide
    if ! echo "$RESPONSE" | grep -q '"_embedded"'; then
        echo -e "${RED}❌ Erreur API ou aucun ticket trouvé${NC}"
        echo "$RESPONSE" | head -5
        return 1
    fi
    
    # Créer le fichier YAML
    local output_file="$TEAM_DIR/tickets/openproject-sync.yaml"
    
    cat > "$output_file" << 'EOF'
# ═══════════════════════════════════════════════════════════════════
# TICKETS - Synchronisé depuis OpenProject
# ═══════════════════════════════════════════════════════════════════
# 
# ⚠️  Ce fichier est généré automatiquement
# Dernière sync: 
EOF
    echo "# $(date '+%Y-%m-%d %H:%M:%S')" >> "$output_file"
    echo "#" >> "$output_file"
    echo "# ═══════════════════════════════════════════════════════════════════" >> "$output_file"
    echo "" >> "$output_file"
    echo "tickets:" >> "$output_file"
    
    # Parser le JSON et générer YAML
    # Utiliser python si disponible pour un parsing propre
    if command -v python3 &> /dev/null; then
        python3 << PYTHON_SCRIPT
import json
import sys

data = json.loads('''$RESPONSE''')

if '_embedded' not in data or 'elements' not in data['_embedded']:
    print("Aucun ticket trouvé", file=sys.stderr)
    sys.exit(0)

tickets = data['_embedded']['elements']
count = 0

for wp in tickets:
    wp_id = wp.get('id', '')
    subject = wp.get('subject', '').replace('"', '\\"')
    description = wp.get('description', {})
    desc_raw = description.get('raw', '') if description else ''
    desc_raw = desc_raw.replace('"', '\\"').replace('\n', ' ')[:200] if desc_raw else ''
    
    # Extraire le type
    wp_type = ''
    if '_links' in wp and 'type' in wp['_links']:
        type_href = wp['_links']['type'].get('href', '')
        wp_type = type_href.split('/')[-1].lower() if type_href else ''
    
    # Extraire le statut
    status = ''
    if '_links' in wp and 'status' in wp['_links']:
        status = wp['_links']['status'].get('title', 'todo').lower()
    
    # Extraire la priorité
    priority = 'medium'
    if '_links' in wp and 'priority' in wp['_links']:
        prio_title = wp['_links']['priority'].get('title', '').lower()
        if 'high' in prio_title or 'urgent' in prio_title:
            priority = 'high'
        elif 'low' in prio_title:
            priority = 'low'
        elif 'critical' in prio_title or 'immediate' in prio_title:
            priority = 'critical'
    
    # Estimation (heures)
    hours = wp.get('estimatedTime', '')
    if hours and hours.startswith('PT'):
        hours = hours.replace('PT', '').replace('H', '')
        try:
            hours = int(float(hours))
        except:
            hours = 4
    else:
        hours = 4
    
    # Convertir statut OpenProject → local
    status_map = {
        'new': 'todo',
        'in progress': 'in_progress',
        'in review': 'review',
        'closed': 'done',
        'rejected': 'done'
    }
    local_status = status_map.get(status, 'todo')
    
    print(f'''
  - id: "OP-{wp_id}"
    openproject_id: {wp_id}
    title: "{subject}"
    type: "{wp_type}"
    priority: "{priority}"
    estimated_hours: {hours}
    status: "{local_status}"
    assignee: null
    description: |
      {desc_raw}''')
    
    count += 1

print(f"\n# Total: {count} tickets", file=sys.stderr)
PYTHON_SCRIPT
    else
        # Fallback sans python - parsing basique
        echo "$RESPONSE" | grep -o '"subject":"[^"]*"' | while read -r line; do
            SUBJECT=$(echo "$line" | cut -d'"' -f4)
            echo ""
            echo "  - id: \"OP-XXX\""
            echo "    title: \"$SUBJECT\""
            echo "    status: \"todo\""
        done
    fi >> "$output_file"
    
    local count=$(grep -c "openproject_id:" "$output_file" 2>/dev/null || echo "0")
    
    echo ""
    echo -e "${GREEN}✓ $count tickets importés${NC}"
    echo -e "Fichier: ${CYAN}$output_file${NC}"
}

# ═══════════════════════════════════════════════════════════════════
# IMPORT FEATURES
# ═══════════════════════════════════════════════════════════════════

import_features() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}              📥 IMPORT FEATURES DEPUIS OPENPROJECT${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    
    local filters=$(build_filters "features")
    local encoded_filters=$(echo "$filters" | sed 's/ /%20/g' | sed 's/\[/%5B/g' | sed 's/\]/%5D/g' | sed 's/{/%7B/g' | sed 's/}/%7D/g' | sed 's/"/%22/g' | sed 's/:/%3A/g' | sed 's/,/%2C/g')
    
    echo -e "${CYAN}Récupération des features...${NC}"
    
    RESPONSE=$(api_get "/work_packages?pageSize=100&filters=$encoded_filters")
    
    if ! echo "$RESPONSE" | grep -q '"_embedded"'; then
        echo -e "${RED}❌ Erreur API ou aucune feature trouvée${NC}"
        return 1
    fi
    
    mkdir -p "$TEAM_DIR/features"
    
    # Parser et créer un fichier par feature
    if command -v python3 &> /dev/null; then
        python3 << PYTHON_SCRIPT
import json
import re
import os

data = json.loads('''$RESPONSE''')

if '_embedded' not in data or 'elements' not in data['_embedded']:
    print("Aucune feature trouvée")
    exit(0)

features = data['_embedded']['elements']
team_dir = "$TEAM_DIR"
count = 0

for wp in features:
    wp_id = wp.get('id', '')
    subject = wp.get('subject', '')
    description = wp.get('description', {})
    desc_raw = description.get('raw', '') if description else ''
    
    # Générer le slug pour le nom de fichier
    slug = re.sub(r'[^a-z0-9]+', '-', subject.lower()).strip('-')[:50]
    
    # Extraire la priorité
    priority = 'medium'
    if '_links' in wp and 'priority' in wp['_links']:
        prio_title = wp['_links']['priority'].get('title', '').lower()
        if 'high' in prio_title or 'urgent' in prio_title:
            priority = 'high'
        elif 'low' in prio_title:
            priority = 'low'
    
    # Estimation (jours)
    hours = wp.get('estimatedTime', '')
    days = 3  # défaut
    if hours and hours.startswith('PT'):
        try:
            h = float(hours.replace('PT', '').replace('H', ''))
            days = max(1, int(h / 8))
        except:
            pass
    
    # Créer le fichier YAML
    filepath = f"{team_dir}/features/{slug}.yaml"
    
    content = f'''# ═══════════════════════════════════════════════════════════════════
# FEATURE: {subject}
# ═══════════════════════════════════════════════════════════════════
# Importé depuis OpenProject #{wp_id}

feature:
  id: "OP-{wp_id}"
  openproject_id: {wp_id}
  name: "{subject}"
  description: |
    {desc_raw[:500] if desc_raw else 'TODO: Ajouter description'}
  
  priority: "{priority}"
  estimated_days: {days}
  status: "todo"

# ═══════════════════════════════════════════════════════════════════
# MODÈLE DE DONNÉES
# ═══════════════════════════════════════════════════════════════════
# TODO: Définir les entités

entities: {{}}

# ═══════════════════════════════════════════════════════════════════
# API REST
# ═══════════════════════════════════════════════════════════════════
# TODO: Définir les endpoints

api:
  endpoints: []

# ═══════════════════════════════════════════════════════════════════
# FRONTEND
# ═══════════════════════════════════════════════════════════════════
# TODO: Définir les pages

frontend:
  pages: []

# ═══════════════════════════════════════════════════════════════════
# NOTES
# ═══════════════════════════════════════════════════════════════════

notes: |
  Importé depuis OpenProject le $(date '+%Y-%m-%d')
  URL: $OPENPROJECT_URL/work_packages/{wp_id}
'''
    
    with open(filepath, 'w') as f:
        f.write(content)
    
    print(f"  ✓ OP-{wp_id}: {subject} → {slug}.yaml")
    count += 1

print(f"\nTotal: {count} features importées")
PYTHON_SCRIPT
    fi
    
    echo ""
    echo -e "${GREEN}✓ Import terminé${NC}"
    echo -e "Fichiers dans: ${CYAN}$TEAM_DIR/features/${NC}"
}

# ═══════════════════════════════════════════════════════════════════
# AIDE
# ═══════════════════════════════════════════════════════════════════

show_help() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}              🔄 SYNC OPENPROJECT${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "${CYAN}Usage:${NC}"
    echo "  ./sync-openproject.sh tickets              Importer Tasks/Bugs"
    echo "  ./sync-openproject.sh features             Importer Features/Epics"
    echo "  ./sync-openproject.sh all                  Importer tout"
    echo ""
    echo -e "${CYAN}Options:${NC}"
    echo "  --project=ID      Filtrer par projet OpenProject"
    echo "  --status=open     Filtrer: open, closed, all (défaut: open)"
    echo ""
    echo -e "${CYAN}Exemples:${NC}"
    echo "  ./sync-openproject.sh tickets --status=open"
    echo "  ./sync-openproject.sh features --project=5"
    echo ""
    echo -e "${CYAN}Types OpenProject → Local:${NC}"
    echo "  Task, Bug         → tickets/openproject-sync.yaml"
    echo "  Feature, Epic     → features/{slug}.yaml"
    echo ""
}

# ═══════════════════════════════════════════════════════════════════
# MAIN
# ═══════════════════════════════════════════════════════════════════

case "$TYPE" in
    tickets)
        import_tickets
        ;;
    features)
        import_features
        ;;
    all)
        import_tickets
        import_features
        ;;
    --help|-h|help|"")
        show_help
        ;;
    *)
        echo -e "${RED}❌ Type inconnu: $TYPE${NC}"
        show_help
        exit 1
        ;;
esac
