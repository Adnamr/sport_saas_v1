#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# IMPORT CSV → YAML (Tickets & Features)
# ═══════════════════════════════════════════════════════════════════
#
# Usage:
#   ./import-csv.sh tickets   fichier.csv    → Importe les tickets
#   ./import-csv.sh features  fichier.csv    → Importe les features
#
# Format CSV Tickets:
#   id,title,module,priority,estimated_hours,description
#
# Format CSV Features:
#   id,name,module,priority,estimated_days,description,entities,endpoints
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

TYPE="$1"
CSV_FILE="$2"

# ═══════════════════════════════════════════════════════════════════
# AIDE
# ═══════════════════════════════════════════════════════════════════

show_help() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}              📥 IMPORT CSV → YAML${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "${CYAN}Usage:${NC}"
    echo "  ./import-csv.sh tickets  fichier.csv"
    echo "  ./import-csv.sh features fichier.csv"
    echo ""
    echo -e "${CYAN}Format CSV Tickets:${NC}"
    echo "  id,title,module,priority,estimated_hours,description"
    echo ""
    echo "  Exemple:"
    echo "  E1-001,Créer entité Product,catalog,high,4,Créer l'entité JPA Product"
    echo "  E1-002,API endpoint GET products,catalog,high,2,Endpoint liste paginée"
    echo ""
    echo -e "${CYAN}Format CSV Features:${NC}"
    echo "  id,name,module,priority,estimated_days,description"
    echo ""
    echo "  Exemple:"
    echo "  F-001,Gestion des Clubs,tenant,high,3,CRUD complet clubs"
    echo "  F-002,Gestion des Membres,tenant,high,3,CRUD membres avec adhésions"
    echo ""
    echo -e "${CYAN}Options:${NC}"
    echo "  --help     Afficher cette aide"
    echo "  --example  Générer des fichiers CSV exemples"
    echo ""
}

# ═══════════════════════════════════════════════════════════════════
# GÉNÉRER EXEMPLES CSV
# ═══════════════════════════════════════════════════════════════════

generate_examples() {
    echo ""
    echo -e "${BLUE}📝 Génération des fichiers CSV exemples...${NC}"
    echo ""
    
    # Exemple tickets
    cat > "$TEAM_DIR/imports/tickets-example.csv" << 'EOF'
id,title,module,priority,estimated_hours,description,acceptance_criteria
E1-001,Créer entité Product,catalog,high,4,"Créer l'entité JPA Product avec tous les champs","Entity créée avec annotations JPA;Validations en place;Tests unitaires OK"
E1-002,Repository ProductRepository,catalog,high,2,"Créer le repository avec méthodes custom","Repository extends JpaRepository;Méthode findByCategory;Méthode search"
E1-003,DTO ProductRequest/Response,catalog,medium,2,"Créer les DTOs pour l'API","Records Java immutables;Validation annotations;Mapping bidirectionnel"
E1-004,Service ProductService,catalog,high,4,"Implémenter le service métier","Interface + Impl;CRUD complet;Gestion exceptions;Tests unitaires"
E1-005,Controller ProductController,catalog,high,3,"Endpoints REST CRUD","GET /products (paginé);GET /products/{id};POST /products;PUT;DELETE"
E2-001,Fix pagination products,catalog,low,1,"La pagination ne fonctionne pas correctement","Page size respecté;Total count correct;Sort fonctionne"
EOF

    # Exemple features
    cat > "$TEAM_DIR/imports/features-example.csv" << 'EOF'
id,name,module,priority,estimated_days,description,main_entities,main_endpoints
F-001,Gestion des Clubs,tenant,high,3,"CRUD complet pour la gestion des clubs sportifs (tenants)","Club;Address;ClubSettings","GET /api/clubs;POST /api/clubs;PUT /api/clubs/{id};DELETE /api/clubs/{id}"
F-002,Gestion des Membres,tenant,high,3,"CRUD membres avec profil et adhésions","Member;Membership;EmergencyContact","GET /api/members;POST /api/members;GET /api/members/me;PUT /api/members/{id}"
F-003,Catalogue Produits,catalog,high,4,"Gestion complète du catalogue équipements sportifs","Product;Category;ProductVariant;ProductImage","GET /api/products;POST /api/products;GET /api/categories"
F-004,Gestion des Stocks,inventory,medium,3,"Suivi des stocks par entrepôt avec alertes","Stock;Warehouse;StockMovement","GET /api/inventory;POST /api/inventory/movements;GET /api/inventory/alerts"
F-005,Réservations,order,high,4,"Système de réservation de matériel","Reservation;ReservationItem;TimeSlot","GET /api/reservations;POST /api/reservations;PATCH /api/reservations/{id}/status"
EOF

    mkdir -p "$TEAM_DIR/imports"
    
    echo -e "${GREEN}✓ Fichiers créés:${NC}"
    echo "  - team/imports/tickets-example.csv"
    echo "  - team/imports/features-example.csv"
    echo ""
    echo -e "${YELLOW}Modifie ces fichiers puis lance:${NC}"
    echo "  make import-tickets FILE=team/imports/tickets-example.csv"
    echo "  make import-features FILE=team/imports/features-example.csv"
    echo ""
}

# ═══════════════════════════════════════════════════════════════════
# IMPORT TICKETS
# ═══════════════════════════════════════════════════════════════════

import_tickets() {
    local csv_file="$1"
    local tickets_dir="$TEAM_DIR/tickets"
    local count=0
    local current_file="$tickets_dir/backlog.yaml"
    
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}              📥 IMPORT TICKETS${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    
    if [[ ! -f "$csv_file" ]]; then
        echo -e "${RED}❌ Fichier non trouvé: $csv_file${NC}"
        exit 1
    fi
    
    # Créer le header du fichier YAML
    cat > "$current_file" << 'EOF'
# ═══════════════════════════════════════════════════════════════════
# BACKLOG TICKETS - Importé depuis CSV
# ═══════════════════════════════════════════════════════════════════
# 
# Statuts: todo | in_progress | review | done
# Priorités: critical | high | medium | low
#
# ═══════════════════════════════════════════════════════════════════

tickets:
EOF

    # Lire le CSV (skip header)
    tail -n +2 "$csv_file" | while IFS=, read -r id title module priority hours description criteria; do
        # Nettoyer les guillemets
        id=$(echo "$id" | tr -d '"')
        title=$(echo "$title" | tr -d '"')
        module=$(echo "$module" | tr -d '"')
        priority=$(echo "$priority" | tr -d '"')
        hours=$(echo "$hours" | tr -d '"')
        description=$(echo "$description" | tr -d '"')
        criteria=$(echo "$criteria" | tr -d '"')
        
        [[ -z "$id" ]] && continue
        
        # Ajouter au YAML
        cat >> "$current_file" << EOF

  - id: "$id"
    title: "$title"
    module: "$module"
    priority: "$priority"
    estimated_hours: $hours
    status: "todo"
    assignee: null
    description: |
      $description
EOF

        # Ajouter critères si présents
        if [[ -n "$criteria" ]]; then
            echo "    acceptance_criteria:" >> "$current_file"
            # Split par ; et ajouter chaque critère
            IFS=';' read -ra CRITERIA_ARRAY <<< "$criteria"
            for crit in "${CRITERIA_ARRAY[@]}"; do
                crit=$(echo "$crit" | xargs)  # trim
                [[ -n "$crit" ]] && echo "      - \"$crit\"" >> "$current_file"
            done
        fi
        
        ((count++)) || true
        echo -e "  ${GREEN}✓${NC} $id: $title"
    done
    
    echo ""
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}✓ Import terminé: $count tickets${NC}"
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "Fichier créé: ${CYAN}$current_file${NC}"
    echo ""
    echo -e "${YELLOW}Commandes:${NC}"
    echo "  make tickets          # Voir les tickets"
    echo "  make ticket TICKET=E1-001 ID=42   # Prendre un ticket"
    echo ""
}

# ═══════════════════════════════════════════════════════════════════
# IMPORT FEATURES
# ═══════════════════════════════════════════════════════════════════

import_features() {
    local csv_file="$1"
    local features_dir="$TEAM_DIR/features"
    local count=0
    
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}              📥 IMPORT FEATURES${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    
    if [[ ! -f "$csv_file" ]]; then
        echo -e "${RED}❌ Fichier non trouvé: $csv_file${NC}"
        exit 1
    fi
    
    mkdir -p "$features_dir"
    
    # Lire le CSV (skip header)
    tail -n +2 "$csv_file" | while IFS=, read -r id name module priority days description entities endpoints; do
        # Nettoyer les guillemets
        id=$(echo "$id" | tr -d '"')
        name=$(echo "$name" | tr -d '"')
        module=$(echo "$module" | tr -d '"')
        priority=$(echo "$priority" | tr -d '"')
        days=$(echo "$days" | tr -d '"')
        description=$(echo "$description" | tr -d '"')
        entities=$(echo "$entities" | tr -d '"')
        endpoints=$(echo "$endpoints" | tr -d '"')
        
        [[ -z "$id" ]] && continue
        
        # Générer le nom de fichier (slug)
        local filename=$(echo "$name" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/--*/-/g' | sed 's/^-//' | sed 's/-$//')
        local filepath="$features_dir/$filename.yaml"
        
        # Créer le fichier YAML de la feature
        cat > "$filepath" << EOF
# ═══════════════════════════════════════════════════════════════════
# FEATURE: $name
# ═══════════════════════════════════════════════════════════════════

feature:
  id: "$id"
  name: "$name"
  description: |
    $description
  
  priority: "$priority"
  estimated_days: $days
  status: "todo"

# ═══════════════════════════════════════════════════════════════════
# MODÈLE DE DONNÉES
# ═══════════════════════════════════════════════════════════════════

entities:
EOF

        # Ajouter les entités
        if [[ -n "$entities" ]]; then
            IFS=';' read -ra ENTITY_ARRAY <<< "$entities"
            for entity in "${ENTITY_ARRAY[@]}"; do
                entity=$(echo "$entity" | xargs)
                [[ -z "$entity" ]] && continue
                cat >> "$filepath" << EOF
  
  $entity:
    description: "TODO: Décrire $entity"
    module: "$module"
    table: "$(echo "$entity" | tr '[:upper:]' '[:lower:]')s"
    fields:
      - name: id
        type: UUID
        generated: true
        
      # TODO: Ajouter les champs
      - name: name
        type: String
        required: true
        
      - name: createdAt
        type: LocalDateTime
        auto: true
        
      - name: updatedAt
        type: LocalDateTime
        auto: true
EOF
            done
        fi

        # Ajouter la section API
        cat >> "$filepath" << EOF

# ═══════════════════════════════════════════════════════════════════
# API REST
# ═══════════════════════════════════════════════════════════════════

api:
  base_path: "/api/$(echo "$module" | tr '[:upper:]' '[:lower:]')"
  module: "$module"
  
  endpoints:
EOF

        # Ajouter les endpoints
        if [[ -n "$endpoints" ]]; then
            IFS=';' read -ra ENDPOINT_ARRAY <<< "$endpoints"
            for endpoint in "${ENDPOINT_ARRAY[@]}"; do
                endpoint=$(echo "$endpoint" | xargs)
                [[ -z "$endpoint" ]] && continue
                
                # Parser "GET /api/xxx"
                local method=$(echo "$endpoint" | awk '{print $1}')
                local path=$(echo "$endpoint" | awk '{print $2}')
                
                cat >> "$filepath" << EOF
    - method: $method
      path: "$path"
      description: "TODO: Décrire"
      roles: [CLUB_ADMIN]
EOF
            done
        fi

        # Ajouter sections frontend et tests
        cat >> "$filepath" << EOF

# ═══════════════════════════════════════════════════════════════════
# FRONTEND
# ═══════════════════════════════════════════════════════════════════

frontend:
  module: "$module"
  
  pages:
    - name: "ListPage"
      route: "/$module"
      type: list
      
    - name: "DetailPage"
      route: "/$module/:id"
      type: detail
      
    - name: "FormPage"
      route: "/$module/new"
      type: form

# ═══════════════════════════════════════════════════════════════════
# TESTS REQUIS
# ═══════════════════════════════════════════════════════════════════

tests:
  unit:
    - "Service.create() - données valides"
    - "Service.update() - entité inexistante → exception"
    
  integration:
    - "GET endpoint - retourne liste paginée"
    - "POST endpoint - crée l'entité"
    - "PUT endpoint - modifie l'entité"

# ═══════════════════════════════════════════════════════════════════
# NOTES
# ═══════════════════════════════════════════════════════════════════

notes: |
  TODO: Compléter les détails de cette feature
  - Définir tous les champs des entités
  - Spécifier les règles métier
  - Détailler les critères d'acceptance
EOF

        ((count++)) || true
        echo -e "  ${GREEN}✓${NC} $id: $name → $filename.yaml"
    done
    
    echo ""
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}✓ Import terminé: $count features${NC}"
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "Fichiers créés dans: ${CYAN}$features_dir/${NC}"
    echo ""
    echo -e "${YELLOW}⚠️  Les features sont des squelettes - complète les détails:${NC}"
    echo "  - Champs des entités"
    echo "  - Paramètres des endpoints"
    echo "  - Pages frontend"
    echo "  - Règles métier"
    echo ""
    echo -e "${YELLOW}Commandes:${NC}"
    echo "  make features                    # Voir les features"
    echo "  make feature FEATURE=xxx         # Démarrer une feature"
    echo ""
}

# ═══════════════════════════════════════════════════════════════════
# MAIN
# ═══════════════════════════════════════════════════════════════════

case "$TYPE" in
    --help|-h|help)
        show_help
        ;;
    --example|example)
        generate_examples
        ;;
    tickets)
        if [[ -z "$CSV_FILE" ]]; then
            echo -e "${RED}❌ Usage: ./import-csv.sh tickets fichier.csv${NC}"
            exit 1
        fi
        import_tickets "$CSV_FILE"
        ;;
    features)
        if [[ -z "$CSV_FILE" ]]; then
            echo -e "${RED}❌ Usage: ./import-csv.sh features fichier.csv${NC}"
            exit 1
        fi
        import_features "$CSV_FILE"
        ;;
    *)
        show_help
        ;;
esac
