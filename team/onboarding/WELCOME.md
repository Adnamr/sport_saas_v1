# 👋 Bienvenue dans l'équipe Sport Equipment SaaS !

Félicitations et bienvenue ! Ce guide va t'aider à démarrer rapidement.

---

## 🎯 Le projet en 30 secondes

**Sport Equipment SaaS** est une plateforme pour gérer du matériel sportif :
- 📦 Catalogue de produits (vélos, raquettes, etc.)
- 📊 Gestion des stocks
- 🛒 Commandes et locations
- 💰 Facturation

C'est une application **multi-tenant** : plusieurs entreprises utilisent la même plateforme, mais chacune ne voit que ses propres données.

---

## 🛠️ Stack technique

| Composant | Technologie |
|-----------|-------------|
| Backend | Java 17 + Spring Boot 3 |
| Base de données | PostgreSQL |
| Migrations | Flyway |
| Frontend | Angular 17+ |
| Conteneurs | Docker |
| CI/CD | GitHub Actions |

---

## 📋 Checklist Jour 1

### Étape 1 : Obtenir les accès
- [ ] Recevoir l'invitation **GitHub** → Accepter
- [ ] Recevoir l'invitation **OpenProject** → Se connecter
- [ ] Rejoindre **Slack/Discord** → Dire bonjour ! 👋

> ⚠️ Si tu n'as pas reçu les accès, contacte ton manager ou voir [team/access/REQUEST-ACCESS.md](../access/REQUEST-ACCESS.md)

### Étape 2 : Installer l'environnement
- [ ] Suivre le guide [SETUP.md](SETUP.md)
- [ ] Vérifier que `make up` fonctionne
- [ ] Vérifier que le backend démarre

### Étape 3 : Lire la documentation
- [ ] [Règles de travail](../rules/WORKFLOW.md) - **Important !**
- [ ] [Architecture](../../docs/architecture/ARCHITECTURE.md) - Vue d'ensemble
- [ ] [Backlog](../../docs/backlog/BACKLOG.md) - Les tickets

### Étape 4 : Premier ticket
- [ ] Demander à ton lead de t'assigner un ticket simple
- [ ] Suivre le guide [FIRST-TICKET.md](FIRST-TICKET.md)

---

## 🗺️ Où trouver quoi ?

```
sport-saas/
├── docs/           → Documentation technique
├── team/           → Guides équipe (tu es ici !)
├── backend/        → Code Java/Spring
├── frontend/       → Code Angular
└── infra/          → Docker, configs
```

---

## ❓ Questions fréquentes

**Je ne comprends pas un ticket**
→ Poser la question en commentaire sur OpenProject ou sur Slack

**Mon environnement ne marche pas**
→ Voir [FAQ.md](FAQ.md) ou demander sur Slack #sport-saas-dev

**Je ne sais pas qui contacter**
→ Voir [team/CONTACTS.md](../CONTACTS.md)

---

## 👥 L'équipe

N'hésite pas à poser des questions, on est tous passés par là ! 

Voir la liste complète dans [CONTACTS.md](../CONTACTS.md).

---

## 📅 Prochaines étapes

1. **Cette semaine** : Setup complet + premier ticket
2. **Semaine 2** : Tickets en autonomie
3. **Semaine 3** : Review de code des autres

Bon courage et bienvenue ! 🎉
