# Sport SaaS - Guide de Déploiement

## Prérequis

- Docker 24.0+
- Docker Compose 2.20+
- 4 GB RAM minimum
- 10 GB espace disque

## Démarrage Rapide

### 1. Cloner le repository

```bash
git clone https://github.com/Adnamr/sport_saas_v1.git
cd sport_saas_v1
```

### 2. Configurer l'environnement

```bash
cp .env.example .env
# Éditer .env avec vos valeurs
```

### 3. Lancer l'application

**Développement (avec MailHog):**
```bash
docker-compose --profile dev up -d
```

**Production:**
```bash
docker-compose up -d
```

### 4. Accéder à l'application

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost | Application Angular |
| Backend API | http://localhost:8080 | API REST Spring Boot |
| Swagger UI | http://localhost:8080/swagger-ui.html | Documentation API |
| MailHog | http://localhost:8025 | Interface emails (dev) |

## Architecture Docker

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │  Frontend   │  │   Backend   │  │  PostgreSQL │     │
│  │   (Nginx)   │──│ (Spring Boot│──│             │     │
│  │    :80      │  │    :8080    │  │   :5432     │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
│                          │                              │
│                   ┌──────┴──────┐                      │
│                   │    Redis    │                      │
│                   │    :6379    │                      │
│                   └─────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

## Commandes Utiles

### Logs
```bash
# Tous les services
docker-compose logs -f

# Service spécifique
docker-compose logs -f backend
```

### Rebuild
```bash
# Rebuild un service
docker-compose build backend

# Rebuild et redémarrer
docker-compose up -d --build
```

### Base de données
```bash
# Accès psql
docker exec -it sport-saas-db psql -U sportsaas -d sportsaas

# Backup
docker exec sport-saas-db pg_dump -U sportsaas sportsaas > backup.sql

# Restore
docker exec -i sport-saas-db psql -U sportsaas sportsaas < backup.sql
```

### Arrêt
```bash
# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

## Configuration Production

### Variables d'environnement critiques

```env
# Sécurité - OBLIGATOIRE à changer
JWT_SECRET=<clé-256-bits-générée>
POSTGRES_PASSWORD=<mot-de-passe-fort>

# Profil Spring
SPRING_PROFILES_ACTIVE=prod
```

### Génération clé JWT
```bash
openssl rand -base64 32
```

### SSL/TLS (recommandé)

Utiliser un reverse proxy (Traefik, Nginx) avec Let's Encrypt:

```yaml
# Exemple avec Traefik labels
labels:
  - "traefik.enable=true"
  - "traefik.http.routers.frontend.rule=Host(`sport-saas.example.com`)"
  - "traefik.http.routers.frontend.tls.certresolver=letsencrypt"
```

## Monitoring

### Health Checks

| Service | Endpoint |
|---------|----------|
| Backend | `GET /actuator/health` |
| Frontend | `GET /health` |

### Métriques

Le backend expose des métriques Prometheus à `/actuator/prometheus` (si activé).

## Troubleshooting

### Le backend ne démarre pas

1. Vérifier les logs: `docker-compose logs backend`
2. Vérifier que PostgreSQL est prêt: `docker-compose logs postgres`
3. Vérifier la connectivité réseau

### Erreur de connexion DB

```bash
# Vérifier que le container DB fonctionne
docker ps | grep postgres

# Tester la connexion
docker exec -it sport-saas-db pg_isready
```

### Problème de permissions

```bash
# Réinitialiser les volumes
docker-compose down -v
docker-compose up -d
```
