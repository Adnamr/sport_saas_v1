# 🎯 Orchestrateur Team - Sport Equipment SaaS

## Concept

Le dossier `team/` sert d'**orchestrateur central** pour piloter Claude Code avec plusieurs développeurs.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│                         ORCHESTRATEUR TEAM                                  │
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │   Dev A     │     │   Dev B     │     │   Dev C     │                   │
│  │ Claude Code │     │ Claude Code │     │ Claude Code │                   │
│  └──────┬──────┘     └──────┬──────┘     └──────┬──────┘                   │
│         │                   │                   │                          │
│         └───────────────────┼───────────────────┘                          │
│                             │                                              │
│                             ▼                                              │
│                    ┌────────────────┐                                      │
│                    │   team/        │                                      │
│                    │                │                                      │
│                    │ • CONFIG       │ ← Règles globales                    │
│                    │ • TICKETS      │ ← Qui travaille sur quoi            │
│                    │ • RULES        │ ← Contraintes à respecter           │
│                    │ • DEVS         │ ← Contexte par développeur          │
│                    │                │                                      │
│                    └────────────────┘                                      │
│                             │                                              │
│                             ▼                                              │
│                    ┌────────────────┐                                      │
│                    │   CLAUDE.md    │ ← Lu par Claude Code                │
│                    │   (généré)     │                                      │
│                    └────────────────┘                                      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Structure

```
sport-saas/
│
├── CLAUDE.md                    # 🤖 Contexte pour Claude Code (GÉNÉRÉ)
│
├── team/                        # 🎯 ORCHESTRATEUR
│   │
│   ├── config.yaml              # ⚙️ Configuration globale
│   │
│   ├── tickets/                 # 🎫 État des tickets
│   │   ├── current.yaml         # Tickets en cours (qui fait quoi)
│   │   ├── blocked.yaml         # Tickets bloqués
│   │   └── completed.yaml       # Historique des tickets terminés
│   │
│   ├── devs/                    # 👤 Contexte par développeur
│   │   ├── alice.yaml           # Config + ticket actuel d'Alice
│   │   ├── bob.yaml             # Config + ticket actuel de Bob
│   │   └── _template.yaml       # Template pour nouveau dev
│   │
│   ├── rules/                   # 📋 Règles et contraintes
│   │   ├── before-ticket.md     # Checklist avant de commencer
│   │   ├── during-ticket.md     # Règles pendant le dev
│   │   ├── code-standards.md    # Standards de code
│   │   └── review-checklist.md  # Checklist pour review
│   │
│   ├── prompts/                 # 💬 Prompts prédéfinis pour Claude
│   │   ├── start-ticket.md      # Prompt pour démarrer un ticket
│   │   ├── entity.md            # Prompt pour créer une entité
│   │   ├── service.md           # Prompt pour créer un service
│   │   ├── controller.md        # Prompt pour créer un controller
│   │   └── fix-bug.md           # Prompt pour corriger un bug
│   │
│   ├── access/                  # 🔐 Gestion des accès
│   │   ├── matrix.yaml          # Qui a accès à quoi
│   │   └── tokens.md            # Guide pour les tokens (pas les vrais!)
│   │
│   └── onboarding/              # 👋 Nouveaux développeurs
│       ├── WELCOME.md
│       ├── SETUP.md
│       └── FIRST-TICKET.md
│
├── backend/                     # Code source
├── frontend/
└── ...
```

---

## 🔄 Workflow avec l'orchestrateur

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│  1. DEV COMMENCE SA JOURNÉE                                                │
│                                                                             │
│     $ cd sport-saas                                                        │
│     $ git pull                                                             │
│     $ cat team/tickets/current.yaml    # Voir qui fait quoi               │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  2. DEV PREND UN TICKET                                                    │
│                                                                             │
│     # Mettre à jour son fichier dev                                        │
│     $ vim team/devs/alice.yaml                                             │
│                                                                             │
│     # Ou utiliser le script                                                │
│     $ ./team/scripts/take-ticket.sh E1-002 alice                          │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  3. CLAUDE CODE LIT LE CONTEXTE                                            │
│                                                                             │
│     Claude Code lit automatiquement:                                       │
│     - CLAUDE.md (règles générales)                                         │
│     - team/devs/alice.yaml (ticket en cours)                              │
│     - team/rules/*.md (contraintes)                                        │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  4. DEV UTILISE UN PROMPT PRÉDÉFINI                                        │
│                                                                             │
│     $ cat team/prompts/entity.md | claude                                  │
│     # Ou copier-coller le prompt                                           │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  5. DEV TERMINE LE TICKET                                                  │
│                                                                             │
│     $ ./team/scripts/complete-ticket.sh E1-002 alice                      │
│     # → Met à jour current.yaml et completed.yaml                         │
│     # → Commit + Push                                                      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Avantages

| Problème | Solution |
|----------|----------|
| Conflits entre devs | `current.yaml` montre qui fait quoi |
| Contexte perdu | `devs/alice.yaml` garde le contexte |
| Règles oubliées | `rules/` appliquées automatiquement |
| Prompts répétitifs | `prompts/` réutilisables |
| Onboarding long | `onboarding/` guide pas à pas |
