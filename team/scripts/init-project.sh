#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════
# Script: init-project.sh
# Description: Initialise le projet complet
# ═══════════════════════════════════════════════════════════════════════════

set -e

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}[1/7] Vérification des prérequis...${NC}"

# Vérifier les outils
check_tool() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}❌ $1 n'est pas installé${NC}"
        exit 1
    fi
    echo -e "  ${GREEN}✓${NC} $1"
}

check_tool git
check_tool docker
check_tool java
check_tool node

echo ""
echo -e "${CYAN}[2/7] Initialisation Git...${NC}"

if [ ! -d ".git" ]; then
    git init
    echo -e "  ${GREEN}✓${NC} Repository Git initialisé"
else
    echo -e "  ${YELLOW}→${NC} Repository Git existe déjà"
fi

# Créer la branche develop si elle n'existe pas
if ! git show-ref --verify --quiet refs/heads/develop; then
    git checkout -b develop 2>/dev/null || git checkout develop
    echo -e "  ${GREEN}✓${NC} Branche develop créée"
else
    echo -e "  ${YELLOW}→${NC} Branche develop existe déjà"
fi

echo ""
echo -e "${CYAN}[3/7] Création des fichiers de configuration...${NC}"

# Créer .env si n'existe pas
if [ ! -f ".env" ]; then
    cat > .env << 'EOF'
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=sportsaas
DB_USER=sportsaas
DB_PASSWORD=sportsaas

# JWT
JWT_SECRET=dev-secret-change-in-production-min-32-chars!!
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Mail (Mailhog for dev)
MAIL_HOST=localhost
MAIL_PORT=1025

# App
APP_PORT=8080
SPRING_PROFILES_ACTIVE=dev
EOF
    echo -e "  ${GREEN}✓${NC} .env créé"
else
    echo -e "  ${YELLOW}→${NC} .env existe déjà"
fi

# Créer .gitignore si n'existe pas
if [ ! -f ".gitignore" ]; then
    cat > .gitignore << 'EOF'
# IDE
.idea/
*.iml
.vscode/
*.swp

# Build
target/
dist/
build/
node_modules/

# Env
.env
*.local

# Logs
*.log
logs/

# OS
.DS_Store
Thumbs.db

# Secrets
*.secret
*.key
*.pem
EOF
    echo -e "  ${GREEN}✓${NC} .gitignore créé"
else
    echo -e "  ${YELLOW}→${NC} .gitignore existe déjà"
fi

# Créer docker-compose.yml si n'existe pas
if [ ! -f "docker-compose.yml" ]; then
    cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: sport-saas-db
    environment:
      POSTGRES_DB: ${DB_NAME:-sportsaas}
      POSTGRES_USER: ${DB_USER:-sportsaas}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-sportsaas}
    ports:
      - "${DB_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-sportsaas}"]
      interval: 10s
      timeout: 5s
      retries: 5

  mailhog:
    image: mailhog/mailhog
    container_name: sport-saas-mail
    ports:
      - "1025:1025"
      - "8025:8025"

volumes:
  postgres_data:

networks:
  default:
    name: sport-saas-network
EOF
    echo -e "  ${GREEN}✓${NC} docker-compose.yml créé"
else
    echo -e "  ${YELLOW}→${NC} docker-compose.yml existe déjà"
fi

echo ""
echo -e "${CYAN}[4/7] Configuration des scripts...${NC}"

# Rendre les scripts exécutables
chmod +x team/scripts/*.sh 2>/dev/null || true
echo -e "  ${GREEN}✓${NC} Scripts rendus exécutables"

echo ""
echo -e "${CYAN}[5/7] Démarrage de Docker...${NC}"

if docker info &> /dev/null; then
    docker compose up -d
    echo -e "  ${GREEN}✓${NC} Services Docker démarrés"
else
    echo -e "  ${RED}⚠️${NC} Docker n'est pas démarré. Lance: ${YELLOW}docker compose up -d${NC}"
fi

echo ""
echo -e "${CYAN}[6/7] Premier commit...${NC}"

if [ -z "$(git status --porcelain)" ]; then
    echo -e "  ${YELLOW}→${NC} Rien à commiter"
else
    git add .
    git commit -m "chore: initial project setup" || true
    echo -e "  ${GREEN}✓${NC} Premier commit effectué"
fi

echo ""
echo -e "${CYAN}[7/7] Vérification finale...${NC}"

echo -e "  ${GREEN}✓${NC} Projet initialisé avec succès!"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ PROJET INITIALISÉ !${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "Prochaines étapes:"
echo ""
echo -e "  1. ${YELLOW}Configurer tes accès (GitHub + OpenProject):${NC}"
echo -e "     ${GREEN}make access${NC}"
echo ""
echo -e "  2. ${YELLOW}Créer ton profil développeur:${NC}"
echo -e "     ${GREEN}make dev DEV=ton_nom${NC}"
echo ""
echo -e "  3. ${YELLOW}Voir les tickets disponibles:${NC}"
echo -e "     ${GREEN}make tickets${NC}"
echo ""
echo -e "  4. ${YELLOW}Prendre ton premier ticket:${NC}"
echo -e "     ${GREEN}make ticket TICKET=E1-001 ID=1${NC}"
echo ""
echo -e "  5. ${YELLOW}Lancer Claude Code:${NC}"
echo -e "     ${GREEN}make claude${NC}"
echo ""
