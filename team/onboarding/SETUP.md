# 🛠️ Installation de l'environnement

Ce guide explique comment installer ton environnement de développement.

---

## 📋 Prérequis

### Logiciels requis

| Logiciel | Version | Vérifier |
|----------|---------|----------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Node.js | 18+ | `node -version` |
| npm | 9+ | `npm -version` |
| Docker | 24+ | `docker -version` |
| Docker Compose | 2+ | `docker compose version` |
| Git | 2.40+ | `git --version` |

### Installation des prérequis

<details>
<summary><b>macOS</b></summary>

```bash
# Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Java 17
brew install openjdk@17
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc

# Node.js
brew install node@18

# Docker Desktop
brew install --cask docker

# Git
brew install git
```
</details>

<details>
<summary><b>Windows</b></summary>

1. **Java 17** : Télécharger depuis [Adoptium](https://adoptium.net/)
2. **Node.js** : Télécharger depuis [nodejs.org](https://nodejs.org/)
3. **Docker Desktop** : Télécharger depuis [docker.com](https://www.docker.com/products/docker-desktop/)
4. **Git** : Télécharger depuis [git-scm.com](https://git-scm.com/)

</details>

<details>
<summary><b>Linux (Ubuntu/Debian)</b></summary>

```bash
# Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs

# Docker
sudo apt install docker.io docker-compose-v2
sudo usermod -aG docker $USER

# Git
sudo apt install git
```
</details>

---

## 🚀 Installation du projet

### 1. Cloner le repository

```bash
git clone https://github.com/[ORG]/sport-saas.git
cd sport-saas
```

### 2. Configurer Git

```bash
git config user.name "Prénom Nom"
git config user.email "ton.email@example.com"
```

### 3. Setup initial

```bash
make setup
```

Cela va :
- Installer les dépendances Maven (backend)
- Installer les dépendances npm (frontend)
- Créer le fichier `.env` depuis `.env.example`

### 4. Démarrer les services Docker

```bash
make up
```

Cela démarre :
- PostgreSQL sur `localhost:5432`
- Mailhog sur `localhost:8025` (interface mail de test)

### 5. Vérifier l'installation

```bash
# Terminal 1 - Backend
make backend

# Attendre "Started Application in X seconds"
# Puis ouvrir http://localhost:8080/swagger-ui.html
```

```bash
# Terminal 2 - Frontend
make frontend

# Attendre "Compiled successfully"
# Puis ouvrir http://localhost:4200
```

---

## ✅ Checklist de vérification

- [ ] `docker compose ps` montre les containers running
- [ ] http://localhost:8080/swagger-ui.html s'affiche
- [ ] http://localhost:4200 s'affiche
- [ ] http://localhost:8025 s'affiche (Mailhog)

---

## 🔧 IDE recommandé

### IntelliJ IDEA (recommandé pour le backend)

1. Ouvrir le dossier `sport-saas/backend`
2. Importer comme projet Maven
3. Plugins recommandés :
   - Lombok
   - Spring Boot Assistant
   - MapStruct Support

### VS Code (recommandé pour le frontend)

1. Ouvrir le dossier `sport-saas/frontend`
2. Extensions recommandées :
   - Angular Language Service
   - ESLint
   - Prettier

---

## ❌ Problèmes courants

### "Port 5432 already in use"
→ Un autre PostgreSQL tourne. Arrête-le ou change le port dans `.env`

### "Docker daemon not running"
→ Démarrer Docker Desktop

### "mvn: command not found"
→ Maven n'est pas dans le PATH. Réinstaller ou configurer le PATH.

### "npm: command not found"
→ Node.js n'est pas dans le PATH. Réinstaller.

### Le backend ne démarre pas
```bash
make down
make db-reset
make up
make backend
```

---

## 📞 Besoin d'aide ?

- Slack : #sport-saas-dev
- Voir [FAQ.md](FAQ.md)
