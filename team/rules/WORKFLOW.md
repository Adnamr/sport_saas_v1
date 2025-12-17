# 🔄 Workflow de Développement

> **Règles à respecter pour chaque ticket**

---

## 📊 Vue d'ensemble

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│   OPENPROJECT              LOCAL                    GITHUB              │
│                                                                         │
│   ┌─────────┐                                                          │
│   │ Trouver │                                                          │
│   │ ticket  │                                                          │
│   │ "Ready" │                                                          │
│   └────┬────┘                                                          │
│        │                                                               │
│        │           ┌────────────────┐                                  │
│        └──────────►│ 1. git pull    │                                  │
│                    │ 2. new branch  │                                  │
│   ┌─────────┐      └────────────────┘                                  │
│   │Assigner │                                                          │
│   │  + "In  │                                                          │
│   │Progress"│                                                          │
│   └────┬────┘                                                          │
│        │           ┌────────────────┐                                  │
│        │           │ 3. Coder       │                                  │
│        │           │ 4. Commits     │                                  │
│        │           └───────┬────────┘                                  │
│        │                   │          ┌─────────────┐                  │
│        │                   └─────────►│ 5. Push     │                  │
│        │                              │ 6. Créer PR │                  │
│   ┌────┴────┐                         └──────┬──────┘                  │
│   │  "In    │◄───────────────────────────────┘                         │
│   │ Review" │                                                          │
│   └────┬────┘                         ┌─────────────┐                  │
│        │                              │ 7. Review   │                  │
│        │                              │ 8. Merge    │                  │
│   ┌────┴────┐                         └──────┬──────┘                  │
│   │"Closed" │◄───────────────────────────────┘                         │
│   └─────────┘                                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🚦 AVANT de prendre un ticket

### 1. Synchroniser le code

```bash
git checkout develop
git pull origin develop
```

### 2. Vérifier sur OpenProject

- [ ] Le ticket a le status **"New"** ou **"Ready"**
- [ ] Le ticket n'est **pas assigné** à quelqu'un d'autre
- [ ] Toutes les **dépendances** sont **"Closed"**

### 3. Ne PAS prendre un ticket si :

| ❌ Situation | Pourquoi |
|-------------|----------|
| Status = "Closed" | Déjà terminé |
| Status = "In Progress" + assigné | Quelqu'un travaille dessus |
| Dépendances pas "Closed" | Tu seras bloqué |

---

## ✅ DÉMARRER un ticket

### Sur OpenProject

1. **S'assigner** le ticket
2. Changer le status → **"In Progress"**

### En local

```bash
# Être sûr d'être à jour
git checkout develop
git pull origin develop

# Créer la branche
git checkout -b feature/<ID>-<TICKET>-<description>
```

**Format de branche :**
```
feature/<ID-OpenProject>-<ID-Ticket>-<description-courte>

Exemples:
  feature/42-E1-002-postgresql
  feature/45-E3-001-user-model
  feature/67-E4-002-product-entity
```

**Raccourci Make :**
```bash
make ticket ID=42 NAME=E1-002-postgresql
```

---

## 💻 PENDANT le développement

### Commits réguliers

**Format :**
```
<type>(<ticket>): <description> - Refs #<ID>
```

**Exemples :**
```bash
git commit -m "feat(E1-002): add postgresql configuration - Refs #42"
git commit -m "feat(E1-002): add flyway migrations - Refs #42"
git commit -m "test(E1-002): add repository tests - Refs #42"
```

**Types :**
| Type | Usage |
|------|-------|
| `feat` | Nouvelle fonctionnalité |
| `fix` | Correction de bug |
| `test` | Ajout/modif de tests |
| `docs` | Documentation |
| `refactor` | Refactoring |
| `chore` | Maintenance |

**Raccourci Make :**
```bash
make qcommit MSG="add postgresql configuration"
# → Détecte automatiquement ID et TICKET depuis la branche
```

### Règles de code

- [ ] Respecter la structure Clean Architecture
- [ ] Les entités héritent de `TenantAwareEntity`
- [ ] Migration Flyway si nouvelle table
- [ ] Pas d'import entre modules (utiliser Events)

---

## 🏁 TERMINER un ticket

### 1. Vérifications locales

```bash
make test   # Tests passent
make lint   # Pas d'erreurs de lint
```

- [ ] Le code compile
- [ ] Les tests passent
- [ ] Pas de `console.log` ou code commenté inutile

### 2. Push et créer la PR

```bash
git push -u origin feature/42-E1-002-postgresql
```

**Sur GitHub :**
- Titre : `[E1-002] Configuration PostgreSQL et Flyway`
- Description : `Refs #42`

**Raccourci Make :**
```bash
make pr
# → Push + affiche le lien pour créer la PR
```

### 3. Mettre à jour OpenProject

- [ ] Changer le status → **"In Review"**
- [ ] Ajouter le lien de la PR en commentaire

### 4. Après le merge

- [ ] Changer le status → **"Closed"**
- [ ] Retourner sur develop

```bash
git checkout develop
git pull origin develop
```

---

## ⚠️ ERREURS À ÉVITER

| ❌ Ne pas faire | ✅ Faire |
|----------------|----------|
| Travailler sans `git pull` | Toujours pull avant de commencer |
| Prendre un ticket bloqué | Vérifier les dépendances |
| Travailler sur "Closed" | Vérifier le status |
| Push sur `develop` direct | Toujours via branche + PR |
| Merger sa propre PR | Attendre une review |
| Commit sans `Refs #ID` | Toujours référencer OpenProject |
| Oublier de changer le status | Mettre à jour à chaque étape |

---

## 📞 En cas de problème

| Problème | Solution |
|----------|----------|
| Conflit Git | Demander de l'aide, ne pas `--force` |
| Ticket bloqué | Identifier la dépendance manquante |
| Question sur ticket | Commenter sur OpenProject |
| Bug après "Closed" | Créer un nouveau ticket "Bug" |

---

## 🔄 Résumé en une image

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  1. git pull                                             │
│  2. Vérifier ticket (Ready + dépendances OK)            │
│  3. S'assigner + "In Progress"                          │
│  4. git checkout -b feature/XX-EX-XXX-xxx               │
│  5. Coder + commits "Refs #XX"                          │
│  6. git push + Créer PR                                 │
│  7. Status → "In Review"                                │
│  8. Review + Merge                                      │
│  9. Status → "Closed"                                   │
│                                                          │
└──────────────────────────────────────────────────────────┘
```
