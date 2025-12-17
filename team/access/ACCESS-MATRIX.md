# 🔐 Matrice des Accès

> Qui a accès à quoi dans le projet

---

## 🛠️ Outils du projet

| Outil | URL | Usage | Admin |
|-------|-----|-------|-------|
| **GitHub** | github.com/[ORG]/sport-saas | Code source, PRs | [Tech Lead] |
| **OpenProject** | aam.openproject.com | Tickets, sprints | [PO/Admin] |
| **Slack** | [workspace].slack.com | Communication | [Admin] |
| **Serveur Dev** | dev.sport-saas.com | Tests | [DevOps] |
| **Serveur Staging** | staging.sport-saas.com | Pré-prod | [DevOps] |

---

## 👥 Accès par personne

| Nom | Rôle | GitHub | OpenProject | Slack | Serveur Dev |
|-----|------|--------|-------------|-------|-------------|
| [Nom 1] | Tech Lead | ✅ Admin | ✅ Manager | ✅ | ✅ |
| [Nom 2] | PO | ❌ | ✅ Admin | ✅ | ❌ |
| [Nom 3] | DevOps | ✅ Admin | ✅ Member | ✅ | ✅ |
| [Nom 4] | Dev Senior | ✅ Maintainer | ✅ Member | ✅ | ✅ |
| [Nom 5] | Dev | ✅ Collaborator | ✅ Member | ✅ | ❌ |
| [Nom 6] | Dev | ✅ Collaborator | ✅ Member | ✅ | ❌ |

---

## 🔑 Niveaux de permissions

### GitHub

| Niveau | Droits |
|--------|--------|
| **Admin** | Tout (settings, secrets, supprimer repo) |
| **Maintainer** | Merger PRs, gérer branches protégées |
| **Collaborator** | Push branches, créer PRs |
| **Read** | Lecture seule |

### OpenProject

| Niveau | Droits |
|--------|--------|
| **Admin** | Tout (membres, settings, supprimer) |
| **Manager** | Créer tickets, assigner, voir tout |
| **Member** | Modifier ses tickets, commenter |
| **Viewer** | Lecture seule |

### Slack

| Niveau | Droits |
|--------|--------|
| **Admin** | Gérer workspace, canaux, membres |
| **Member** | Participer aux canaux |
| **Guest** | Accès limité à certains canaux |

---

## 📝 Droits par défaut pour les nouveaux

| Outil | Niveau par défaut |
|-------|-------------------|
| GitHub | Collaborator |
| OpenProject | Member |
| Slack | Member |
| Serveur Dev | ❌ (sur demande) |

---

## 🔒 Règles de sécurité

### Obligatoire

- ✅ **2FA activé** sur GitHub (obligatoire)
- ✅ **Mot de passe fort** (12+ caractères)
- ✅ **Ne jamais partager** son mot de passe ou token

### Interdit

- ❌ Partager ses identifiants
- ❌ Commit de secrets dans le code
- ❌ Accéder aux ressources non autorisées

### En cas de problème

Si tu penses que ton compte est compromis :
1. **Changer immédiatement** ton mot de passe
2. **Révoquer** les tokens actifs
3. **Prévenir** l'admin (voir [CONTACTS.md](../CONTACTS.md))

---

## 📊 Historique des accès

| Date | Action | Personne | Par |
|------|--------|----------|-----|
| JJ/MM/AAAA | Ajout | [Nom] | [Admin] |
| JJ/MM/AAAA | Retrait | [Nom] | [Admin] |

---

## ➡️ Besoin d'un accès ?

Voir [REQUEST-ACCESS.md](REQUEST-ACCESS.md)

## ➡️ Quelqu'un part ?

Voir [OFFBOARDING.md](OFFBOARDING.md)
