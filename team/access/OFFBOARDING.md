# 🚪 Procédure de Départ (Offboarding)

> Checklist quand quelqu'un quitte le projet

---

## 📋 Checklist Admin

Quand quelqu'un quitte le projet, l'admin doit :

### Dans les 24h

- [ ] **GitHub** : Retirer du repository
- [ ] **OpenProject** : Désactiver le compte
- [ ] **Slack** : Retirer du workspace
- [ ] **Serveurs** : Révoquer les clés SSH
- [ ] **Tokens API** : Révoquer tous les tokens

### Dans la semaine

- [ ] **Réassigner** les tickets en cours
- [ ] **Archiver** ou merger les branches en cours
- [ ] **Mettre à jour** [ACCESS-MATRIX.md](ACCESS-MATRIX.md)
- [ ] **Informer** l'équipe si nécessaire

---

## 📋 Checklist Développeur qui part

Avant ton dernier jour :

### Code

- [ ] **Push** toutes tes branches locales
- [ ] **Documenter** le travail en cours (commentaires sur tickets)
- [ ] **Informer** ton lead de l'état de tes tickets

### Passation

- [ ] **Réunion** de passation avec l'équipe
- [ ] **Transférer** les connaissances critiques
- [ ] **Répondre** aux dernières questions

### Accès

- [ ] **Révoquer** tes tokens personnels (GitHub, OpenProject)
- [ ] **Supprimer** le projet de ta machine (optionnel mais recommandé)

---

## 📊 Template de passation

```markdown
# Passation - [Nom]

## Tickets en cours

| Ticket | Status | Branche | Notes |
|--------|--------|---------|-------|
| #42 E1-002 | In Progress | feature/42-E1-002 | En attente review |
| #45 E3-001 | In Review | feature/45-E3-001 | PR #12 ouverte |

## Points d'attention

- [Point important 1]
- [Point important 2]

## Connaissances à transférer

- [Sujet 1] → Transféré à [Nom]
- [Sujet 2] → Documenté dans [fichier]

## Contacts externes

- [Contact 1] pour [sujet]
- [Contact 2] pour [sujet]
```

---

## ⏱️ Timeline

| Moment | Action |
|--------|--------|
| J-7 | Annonce du départ à l'équipe |
| J-5 | Début de la passation |
| J-2 | Finaliser la documentation |
| J-1 | Derniers push, dernières questions |
| J | Révocation des accès |

---

## 👤 Responsable

L'offboarding est géré par : **[Nom de l'admin]**

Contact : @slack ou email
