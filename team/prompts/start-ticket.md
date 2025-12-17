# 🚀 Prompt: Démarrer un ticket

> Copie ce prompt et adapte les valeurs entre `{...}`

---

## Prompt à copier:

```
Je démarre le ticket {TICKET_ID} - {TITRE}

## Contexte
- Module: {MODULE}
- Sprint: {SPRINT}
- Dépendances terminées: {DEPS}

## Critères d'acceptation
{COPIER_DEPUIS_OPENPROJECT}

## Ce que tu dois faire

1. Lire le fichier CLAUDE.md pour le contexte projet
2. Vérifier les dépendances dans team/tickets/current.yaml
3. Générer le code selon les standards du projet
4. Créer les fichiers dans le bon module
5. Créer la migration Flyway si nécessaire
6. Générer les tests unitaires

## Contraintes à respecter
- Clean Architecture (api/domain/infra)
- L'entité hérite de TenantAwareEntity
- Pas d'import entre modules
- Format des commits: feat({TICKET_ID}): ... - Refs #{OPENPROJECT_ID}

Commence par me montrer la liste des fichiers à créer.
```

---

## Exemple rempli:

```
Je démarre le ticket E1-002 - PostgreSQL et Flyway

## Contexte
- Module: config
- Sprint: Sprint 1
- Dépendances terminées: E1-001 ✅

## Critères d'acceptation
- [ ] Connexion PostgreSQL fonctionnelle
- [ ] Flyway configuré et exécute les migrations
- [ ] Script de création de la base initiale
- [ ] Configuration par environnement (dev, prod)

## Ce que tu dois faire

1. Lire le fichier CLAUDE.md pour le contexte projet
2. Vérifier les dépendances dans team/tickets/current.yaml
3. Générer le code selon les standards du projet
4. Créer les fichiers dans le bon module
5. Créer la migration Flyway si nécessaire
6. Générer les tests unitaires

## Contraintes à respecter
- Clean Architecture (api/domain/infra)
- L'entité hérite de TenantAwareEntity
- Pas d'import entre modules
- Format des commits: feat(E1-002): ... - Refs #42

Commence par me montrer la liste des fichiers à créer.
```

---

## Checklist avant d'envoyer ce prompt

- [ ] J'ai fait `git pull`
- [ ] J'ai vérifié que le ticket est READY dans `team/tickets/current.yaml`
- [ ] J'ai mis à jour mon fichier `team/devs/{moi}.yaml`
- [ ] J'ai créé ma branche: `git checkout -b feature/{ID}-{TICKET}-xxx`
