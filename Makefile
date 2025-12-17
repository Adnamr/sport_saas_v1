# ═══════════════════════════════════════════════════════════════════════════
#                    SPORT EQUIPMENT SAAS - MAKEFILE
# ═══════════════════════════════════════════════════════════════════════════
#
# Usage:
#   make help              → Afficher l'aide
#   make init              → Initialiser le projet complet
#   make dev               → Configurer un nouveau développeur
#   make ticket            → Prendre un ticket
#   make done              → Terminer un ticket
#   make status            → Voir l'état du projet
#
# ═══════════════════════════════════════════════════════════════════════════

.PHONY: help init dev ticket done status up down logs backend frontend test clean

# Couleurs
GREEN  := \033[0;32m
YELLOW := \033[1;33m
BLUE   := \033[0;34m
RED    := \033[0;31m
CYAN   := \033[0;36m
NC     := \033[0m

# Variables par défaut
DEV ?= $(shell whoami)
TICKET ?= 
ID ?= 0
MSG ?= 

# ═══════════════════════════════════════════════════════════════════════════
#                              AIDE
# ═══════════════════════════════════════════════════════════════════════════

help:
	@echo ""
	@echo "$(BLUE)═══════════════════════════════════════════════════════════════$(NC)"
	@echo "$(BLUE)              🏀 SPORT EQUIPMENT SAAS - COMMANDES$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(CYAN)🚀 INITIALISATION (dans cet ordre)$(NC)"
	@echo "   $(GREEN)make init$(NC)                    1. Initialiser le projet"
	@echo "   $(GREEN)make access$(NC)                  2. Configurer GitHub + OpenProject"
	@echo "   $(GREEN)make dev DEV=alice$(NC)           3. Créer ton profil développeur"
	@echo ""
	@echo "$(CYAN)🎫 TICKETS (petits fixes/bugs)$(NC)"
	@echo "   $(GREEN)make status$(NC)                  État du projet (tickets, devs)"
	@echo "   $(GREEN)make tickets$(NC)                 Liste des tickets disponibles"
	@echo "   $(GREEN)make ticket TICKET=E1-002 ID=42$(NC)  Prendre un ticket"
	@echo "   $(GREEN)make work$(NC)                    ⭐ Claude + Commit + PR (tout en un!)"
	@echo "   $(GREEN)make done$(NC)                    Terminer le ticket (PR + OpenProject)"
	@echo ""
	@echo "$(CYAN)🚀 FEATURES (bout en bout)$(NC)"
	@echo "   $(GREEN)make features$(NC)                Liste des features disponibles"
	@echo "   $(GREEN)make feature FEATURE=clubs$(NC)   ⭐ Démarrer une feature complète"
	@echo "   $(GREEN)make feature-done$(NC)            Terminer la feature (tests + PR)"
	@echo ""
	@echo "$(CYAN)📥 IMPORT CSV$(NC)"
	@echo "   $(GREEN)make import-example$(NC)          Générer des CSV exemples"
	@echo "   $(GREEN)make import-tickets FILE=x.csv$(NC)   Importer tickets depuis CSV"
	@echo "   $(GREEN)make import-features FILE=x.csv$(NC)  Importer features depuis CSV"
	@echo ""
	@echo "$(CYAN)🔄 SYNC OPENPROJECT$(NC)"
	@echo "   $(GREEN)make op-sync-tickets$(NC)         Importer tickets depuis OpenProject API"
	@echo "   $(GREEN)make op-sync-features$(NC)        Importer features depuis OpenProject API"
	@echo "   $(GREEN)make op-sync$(NC)                 Importer tout depuis OpenProject API"
	@echo "   $(GREEN)make import-openproject FILE=x.csv$(NC)  ⭐ Convertir CSV OpenProject → Features"
	@echo ""
	@echo "$(CYAN)🔄 SYNCHRONISATION$(NC)"
	@echo "   $(GREEN)make sync$(NC)                    Retour sur develop après merge"
	@echo ""
	@echo "$(CYAN)💻 DÉVELOPPEMENT$(NC)"
	@echo "   $(GREEN)make up$(NC)                      Démarrer Docker (PostgreSQL)"
	@echo "   $(GREEN)make down$(NC)                    Arrêter Docker"
	@echo "   $(GREEN)make backend$(NC)                 Lancer le backend"
	@echo "   $(GREEN)make frontend$(NC)                Lancer le frontend"
	@echo "   $(GREEN)make test$(NC)                    Lancer les tests"
	@echo ""
	@echo "$(CYAN)📝 GIT$(NC)"
	@echo "   $(GREEN)make commit MSG=\"message\"$(NC)    Commit formaté automatique"
	@echo "   $(GREEN)make push$(NC)                    Push + lien PR"
	@echo "   $(GREEN)make pr$(NC)                      Ouvrir la PR sur GitHub"
	@echo ""
	@echo "$(CYAN)📊 OPENPROJECT$(NC)"
	@echo "   $(GREEN)make op-status ID=42$(NC)         Voir le statut d'un ticket"
	@echo "   $(GREEN)make op-start ID=42$(NC)          Passer en 'In Progress'"
	@echo "   $(GREEN)make op-review ID=42$(NC)         Passer en 'In Review'"
	@echo "   $(GREEN)make op-close ID=42$(NC)          Passer en 'Closed'"
	@echo "   $(GREEN)make op-my$(NC)                   Lister mes tickets"
	@echo ""
	@echo "$(CYAN)🤖 CLAUDE CODE$(NC)"
	@echo "   $(GREEN)make claude$(NC)                  Lancer Claude avec contexte"
	@echo "   $(GREEN)make prompt TYPE=entity$(NC)      Afficher un prompt (entity/service/api)"
	@echo ""
	@echo "$(CYAN)📚 DOCUMENTATION$(NC)"
	@echo "   $(GREEN)make info$(NC)                    Informations du projet"
	@echo "   $(GREEN)make urls$(NC)                    URLs utiles"
	@echo ""

# ═══════════════════════════════════════════════════════════════════════════
#                           INITIALISATION
# ═══════════════════════════════════════════════════════════════════════════

init:
	@echo ""
	@echo "$(BLUE)═══════════════════════════════════════════════════════════════$(NC)"
	@echo "$(BLUE)              🚀 INITIALISATION DU PROJET$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════════════$(NC)"
	@echo ""
	@./team/scripts/init-project.sh

access:
	@./team/scripts/setup-access.sh $(DEV)

check-access:
	@./team/scripts/check-access.sh

dev:
	@echo ""
	@echo "$(BLUE)═══════════════════════════════════════════════════════════════$(NC)"
	@echo "$(BLUE)              👤 CONFIGURATION DÉVELOPPEUR: $(DEV)$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════════════$(NC)"
	@echo ""
	@./team/scripts/check-access.sh --quiet || (echo "$(RED)❌ Configure d'abord tes accès: make access$(NC)" && exit 1)
	@./team/scripts/setup-dev.sh $(DEV)

# ═══════════════════════════════════════════════════════════════════════════
#                         GESTION DES TICKETS
# ═══════════════════════════════════════════════════════════════════════════

status:
	@./team/scripts/show-status.sh

tickets:
	@./team/scripts/list-tickets.sh

ticket:
ifndef TICKET
	@echo "$(RED)❌ Usage: make ticket TICKET=E1-002 ID=42$(NC)"
	@echo ""
	@echo "Tickets disponibles:"
	@./team/scripts/list-tickets.sh
else
	@./team/scripts/take-ticket.sh $(TICKET) $(DEV) $(ID)
endif

done:
	@./team/scripts/complete-ticket.sh $(DEV)

work:
	@./team/scripts/work.sh $(DEV)

sync:
	@echo "$(YELLOW)🔄 Synchronisation avec develop...$(NC)"
	@git fetch origin
	@git checkout develop
	@git pull origin develop
	@echo "$(GREEN)✓ Synchronisé$(NC)"

# ═══════════════════════════════════════════════════════════════════════════
#                         FEATURES (bout en bout)
# ═══════════════════════════════════════════════════════════════════════════

features:
	@./team/scripts/list-features.sh

feature:
ifndef FEATURE
	@./team/scripts/list-features.sh
else
	@./team/scripts/start-feature.sh $(FEATURE) $(DEV)
endif

feature-done:
	@./team/scripts/complete-feature.sh $(DEV)

# ═══════════════════════════════════════════════════════════════════════════
#                         IMPORT CSV
# ═══════════════════════════════════════════════════════════════════════════

import-example:
	@./team/scripts/import-csv.sh --example

import-tickets:
ifndef FILE
	@echo "$(RED)❌ Usage: make import-tickets FILE=chemin/vers/tickets.csv$(NC)"
	@echo ""
	@echo "Format CSV attendu:"
	@echo "  id,title,module,priority,estimated_hours,description,acceptance_criteria"
	@echo ""
	@echo "Exemple: make import-example  → génère des fichiers CSV exemples"
else
	@./team/scripts/import-csv.sh tickets $(FILE)
endif

import-features:
ifndef FILE
	@echo "$(RED)❌ Usage: make import-features FILE=chemin/vers/features.csv$(NC)"
	@echo ""
	@echo "Format CSV attendu:"
	@echo "  id,name,module,priority,estimated_days,description,main_entities,main_endpoints"
	@echo ""
	@echo "Exemple: make import-example  → génère des fichiers CSV exemples"
else
	@./team/scripts/import-csv.sh features $(FILE)
endif

# ═══════════════════════════════════════════════════════════════════════════
#                         SYNC OPENPROJECT
# ═══════════════════════════════════════════════════════════════════════════

op-sync-tickets:
	@./team/scripts/sync-openproject.sh tickets

op-sync-features:
	@./team/scripts/sync-openproject.sh features

op-sync:
	@./team/scripts/sync-openproject.sh all

import-openproject:
ifndef FILE
	@echo "$(RED)❌ Usage: make import-openproject FILE=export-openproject.csv$(NC)"
	@echo ""
	@echo "Exporte ton backlog OpenProject en CSV puis lance cette commande."
	@echo "Le script va regrouper les tickets par Epic (E1, E2, etc.) en features."
else
	@python3 team/scripts/openproject-to-features.py $(FILE) team/features/
endif

# ═══════════════════════════════════════════════════════════════════════════
#                            DOCKER
# ═══════════════════════════════════════════════════════════════════════════

up:
	@echo "$(YELLOW)🐳 Démarrage des services Docker...$(NC)"
	@docker compose up -d
	@echo "$(GREEN)✓ Services démarrés$(NC)"
	@make urls

down:
	@echo "$(YELLOW)🐳 Arrêt des services Docker...$(NC)"
	@docker compose down
	@echo "$(GREEN)✓ Services arrêtés$(NC)"

logs:
	@docker compose logs -f

db-reset:
	@echo "$(YELLOW)🗄️ Reset de la base de données...$(NC)"
	@docker compose down -v
	@docker compose up -d postgres
	@echo "$(GREEN)✓ Base de données réinitialisée$(NC)"

# ═══════════════════════════════════════════════════════════════════════════
#                          DÉVELOPPEMENT
# ═══════════════════════════════════════════════════════════════════════════

backend:
	@echo "$(YELLOW)☕ Démarrage du backend...$(NC)"
	@cd backend && ./mvnw spring-boot:run -pl app -Dspring-boot.run.profiles=dev

frontend:
	@echo "$(YELLOW)🎨 Démarrage du frontend...$(NC)"
	@cd frontend && npm start

test:
	@echo "$(YELLOW)🧪 Lancement des tests...$(NC)"
	@cd backend && ./mvnw test

test-backend:
	@cd backend && ./mvnw test

test-frontend:
	@cd frontend && npm test

build:
	@echo "$(YELLOW)🔨 Build du projet...$(NC)"
	@cd backend && ./mvnw clean package -DskipTests

# ═══════════════════════════════════════════════════════════════════════════
#                               GIT
# ═══════════════════════════════════════════════════════════════════════════

commit:
ifndef MSG
	@echo "$(RED)❌ Usage: make commit MSG=\"description du commit\"$(NC)"
else
	@./team/scripts/smart-commit.sh "$(MSG)"
endif

push:
	@./team/scripts/smart-push.sh

pr:
	@./team/scripts/open-pr.sh

# ═══════════════════════════════════════════════════════════════════════════
#                            OPENPROJECT
# ═══════════════════════════════════════════════════════════════════════════

op-status:
ifndef ID
	@echo "$(RED)❌ Usage: make op-status ID=42$(NC)"
else
	@./team/scripts/openproject-api.sh status $(ID)
endif

op-start:
ifndef ID
	@echo "$(RED)❌ Usage: make op-start ID=42$(NC)"
else
	@./team/scripts/openproject-api.sh start $(ID)
endif

op-review:
ifndef ID
	@echo "$(RED)❌ Usage: make op-review ID=42$(NC)"
else
	@./team/scripts/openproject-api.sh review $(ID)
endif

op-close:
ifndef ID
	@echo "$(RED)❌ Usage: make op-close ID=42$(NC)"
else
	@./team/scripts/openproject-api.sh close $(ID)
endif

op-assign:
ifndef ID
	@echo "$(RED)❌ Usage: make op-assign ID=42$(NC)"
else
	@./team/scripts/openproject-api.sh assign $(ID)
endif

op-my:
	@./team/scripts/openproject-api.sh list

op-test:
	@./team/scripts/openproject-api.sh test

# ═══════════════════════════════════════════════════════════════════════════
#                            CLAUDE CODE
# ═══════════════════════════════════════════════════════════════════════════

claude:
	@echo "$(BLUE)🤖 Lancement de Claude Code avec contexte...$(NC)"
	@./team/scripts/launch-claude.sh $(DEV)

prompt:
ifndef TYPE
	@echo "$(YELLOW)Prompts disponibles:$(NC)"
	@ls -1 team/prompts/ | sed 's/.md//' | sed 's/^/  - /'
	@echo ""
	@echo "Usage: $(GREEN)make prompt TYPE=entity$(NC)"
else
	@cat team/prompts/$(TYPE).md 2>/dev/null || cat team/prompts/create-$(TYPE).md 2>/dev/null || echo "$(RED)❌ Prompt '$(TYPE)' non trouvé$(NC)"
endif

# ═══════════════════════════════════════════════════════════════════════════
#                           INFORMATIONS
# ═══════════════════════════════════════════════════════════════════════════

info:
	@echo ""
	@echo "$(BLUE)═══════════════════════════════════════════════════════════════$(NC)"
	@echo "$(BLUE)              📋 INFORMATIONS PROJET$(NC)"
	@echo "$(BLUE)═══════════════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(CYAN)Projet:$(NC) Sport Equipment SaaS"
	@echo "$(CYAN)Stack:$(NC)  Java 17 + Spring Boot 3.3 | Angular 17 | PostgreSQL"
	@echo ""
	@echo "$(CYAN)Branche actuelle:$(NC) $$(git branch --show-current)"
	@echo "$(CYAN)Développeur:$(NC)      $(DEV)"
	@echo ""
	@./team/scripts/show-current-ticket.sh $(DEV)
	@echo ""

urls:
	@echo ""
	@echo "$(CYAN)🔗 URLs:$(NC)"
	@echo "   Backend:     http://localhost:8080"
	@echo "   Swagger:     http://localhost:8080/swagger-ui.html"
	@echo "   Frontend:    http://localhost:4200"
	@echo "   PostgreSQL:  localhost:5432"
	@echo "   Mailhog:     http://localhost:8025"
	@echo "   OpenProject: https://aam.openproject.com/"
	@echo ""

# ═══════════════════════════════════════════════════════════════════════════
#                             NETTOYAGE
# ═══════════════════════════════════════════════════════════════════════════

clean:
	@echo "$(YELLOW)🧹 Nettoyage...$(NC)"
	@cd backend && ./mvnw clean || true
	@cd frontend && rm -rf node_modules/.cache || true
	@echo "$(GREEN)✓ Nettoyé$(NC)"

clean-all: clean
	@docker compose down -v
	@cd frontend && rm -rf node_modules || true
	@echo "$(GREEN)✓ Tout nettoyé$(NC)"
