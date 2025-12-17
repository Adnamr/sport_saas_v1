#!/usr/bin/env python3
"""
Convertit un export CSV OpenProject en fichiers YAML features.

Usage:
    python3 openproject-to-features.py input.csv [output_dir]

Format CSV attendu (export OpenProject):
    Subject,Type,Status,Priority,Assignee,Version,Estimated time,Description,Parent,Follows
"""

import csv
import re
import sys
import os
from collections import defaultdict
from datetime import datetime

def parse_time(time_str):
    """Convertit '3h' en heures numériques."""
    if not time_str:
        return 0
    match = re.search(r'(\d+)', time_str)
    return int(match.group(1)) if match else 0

def extract_module(description):
    """Extrait le module depuis la description."""
    match = re.search(r'\*\*Module:\*\*\s*(.+?)(?:\n|$)', description)
    if match:
        modules = [m.strip() for m in match.group(1).split(',')]
        return modules[0] if modules else 'common'
    return 'common'

def extract_acceptance_criteria(description):
    """Extrait les critères d'acceptation."""
    criteria = []
    in_criteria = False
    
    for line in description.split('\n'):
        if 'Critères d\'acceptation' in line or 'acceptance' in line.lower():
            in_criteria = True
            continue
        if in_criteria:
            if line.startswith('**') and 'Module' not in line:
                break
            match = re.match(r'[-\[\]x ]*(.+)', line.strip())
            if match and match.group(1).strip():
                criteria.append(match.group(1).strip())
    
    return criteria

def extract_entities(description, subject):
    """Extrait les entités potentielles."""
    entities = set()
    
    # Patterns courants d'entités
    patterns = [
        r'entit[ée]\s+(\w+)',
        r'table\s+(\w+)',
        r'Entity\s+(\w+)',
        r'model\s+(\w+)',
        r'classe?\s+(\w+)',
    ]
    
    text = description + ' ' + subject
    for pattern in patterns:
        matches = re.findall(pattern, text, re.IGNORECASE)
        entities.update(m for m in matches if len(m) > 2)
    
    # Entités connues sport-saas
    known_entities = ['Product', 'Category', 'Stock', 'Order', 'User', 'Tenant', 
                      'Club', 'Member', 'Invoice', 'Cart', 'Warehouse', 'Movement']
    for entity in known_entities:
        if entity.lower() in text.lower():
            entities.add(entity)
    
    return list(entities)

def extract_endpoints(description):
    """Extrait les endpoints API."""
    endpoints = []
    patterns = [
        r'(GET|POST|PUT|PATCH|DELETE)\s+(/[a-z/_{}]+)',
        r'endpoint[s]?\s+(/[a-z/_{}]+)',
    ]
    
    for pattern in patterns:
        matches = re.findall(pattern, description, re.IGNORECASE)
        for match in matches:
            if isinstance(match, tuple):
                endpoints.append(f"{match[0]} {match[1]}")
            else:
                endpoints.append(match)
    
    return endpoints

def slugify(text):
    """Convertit un texte en slug."""
    text = text.lower()
    text = re.sub(r'[àáâãäå]', 'a', text)
    text = re.sub(r'[èéêë]', 'e', text)
    text = re.sub(r'[ìíîï]', 'i', text)
    text = re.sub(r'[òóôõö]', 'o', text)
    text = re.sub(r'[ùúûü]', 'u', text)
    text = re.sub(r'[ç]', 'c', text)
    text = re.sub(r'[^a-z0-9]+', '-', text)
    text = re.sub(r'-+', '-', text)
    return text.strip('-')[:50]

def guess_feature_name(tickets):
    """Devine le nom de la feature à partir des tickets."""
    # Chercher des patterns communs
    subjects = [t['subject'] for t in tickets]
    
    # Mots-clés communs
    keywords = defaultdict(int)
    for subject in subjects:
        words = re.findall(r'\b[A-Z][a-z]+\b', subject)
        for word in words:
            if word not in ['Setup', 'Create', 'Configure', 'Add', 'Module', 'Entity', 'API', 'Service']:
                keywords[word] += 1
    
    if keywords:
        top_keyword = max(keywords, key=keywords.get)
        return f"Module {top_keyword}"
    
    # Sinon utiliser le premier ticket
    return subjects[0] if subjects else "Feature"

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 openproject-to-features.py input.csv [output_dir]")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_dir = sys.argv[2] if len(sys.argv) > 2 else './features'
    
    os.makedirs(output_dir, exist_ok=True)
    
    # Lire le CSV
    epics = defaultdict(list)
    
    with open(input_file, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        
        for row in reader:
            subject = row.get('Subject', '')
            
            # Extraire l'ID epic [E1-001]
            match = re.match(r'\[E(\d+)-(\d+)\]\s*(.+)', subject)
            if match:
                epic_num = match.group(1)
                ticket_num = match.group(2)
                title = match.group(3).strip()
                
                ticket = {
                    'id': f"E{epic_num}-{ticket_num}",
                    'epic': f"E{epic_num}",
                    'subject': title,
                    'full_subject': subject,
                    'type': row.get('Type', 'Task'),
                    'status': row.get('Status', 'New'),
                    'priority': row.get('Priority', 'Medium'),
                    'sprint': row.get('Version', ''),
                    'estimated_hours': parse_time(row.get('Estimated time', '')),
                    'description': row.get('Description', ''),
                    'parent': row.get('Parent', ''),
                    'follows': row.get('Follows', ''),
                    'module': extract_module(row.get('Description', '')),
                    'criteria': extract_acceptance_criteria(row.get('Description', '')),
                }
                
                epics[epic_num].append(ticket)
    
    print(f"\n{'='*60}")
    print(f"  CONVERSION OPENPROJECT → FEATURES")
    print(f"{'='*60}\n")
    
    # Générer un fichier YAML par epic
    for epic_num, tickets in sorted(epics.items(), key=lambda x: int(x[0])):
        # Calculer les stats
        total_hours = sum(t['estimated_hours'] for t in tickets)
        estimated_days = max(1, total_hours // 8)
        
        # Deviner le nom de la feature
        feature_name = guess_feature_name(tickets)
        
        # Extraire entités et endpoints
        all_entities = set()
        all_endpoints = []
        modules = set()
        all_criteria = []
        
        for ticket in tickets:
            all_entities.update(extract_entities(ticket['description'], ticket['subject']))
            all_endpoints.extend(extract_endpoints(ticket['description']))
            modules.add(ticket['module'])
            all_criteria.extend(ticket['criteria'])
        
        # Priorité globale
        priorities = [t['priority'].lower() for t in tickets]
        if 'high' in priorities or 'immediate' in priorities:
            priority = 'high'
        elif 'low' in priorities:
            priority = 'low'
        else:
            priority = 'medium'
        
        # Slug pour le nom de fichier
        slug = slugify(feature_name)
        filename = f"{output_dir}/e{epic_num}-{slug}.yaml"
        
        # Générer le YAML
        yaml_content = f'''# ═══════════════════════════════════════════════════════════════════
# FEATURE E{epic_num}: {feature_name}
# ═══════════════════════════════════════════════════════════════════
# Généré depuis OpenProject le {datetime.now().strftime('%Y-%m-%d %H:%M')}
# Tickets: {len(tickets)} | Estimation: {total_hours}h (~{estimated_days} jours)

feature:
  id: "E{epic_num}"
  name: "{feature_name}"
  description: |
    Feature regroupant {len(tickets)} tickets de l'epic E{epic_num}.
    Modules concernés: {', '.join(sorted(modules))}
  
  priority: "{priority}"
  estimated_days: {estimated_days}
  status: "todo"

# ═══════════════════════════════════════════════════════════════════
# TICKETS INCLUS
# ═══════════════════════════════════════════════════════════════════

tickets:
'''
        
        for ticket in sorted(tickets, key=lambda t: t['id']):
            yaml_content += f'''
  - id: "{ticket['id']}"
    title: "{ticket['subject']}"
    module: "{ticket['module']}"
    priority: "{ticket['priority'].lower()}"
    estimated_hours: {ticket['estimated_hours']}
    sprint: "{ticket['sprint']}"
    status: "{ticket['status'].lower().replace(' ', '_')}"
'''
            if ticket['criteria']:
                yaml_content += "    acceptance_criteria:\n"
                for crit in ticket['criteria'][:5]:  # Max 5 critères
                    crit_clean = crit.replace('"', "'").replace('\n', ' ')[:100]
                    yaml_content += f'      - "{crit_clean}"\n'
        
        # Section entités
        if all_entities:
            yaml_content += f'''
# ═══════════════════════════════════════════════════════════════════
# ENTITÉS DÉTECTÉES
# ═══════════════════════════════════════════════════════════════════

entities:
'''
            for entity in sorted(all_entities):
                yaml_content += f'''
  {entity}:
    description: "TODO: Décrire {entity}"
    module: "{list(modules)[0] if modules else 'common'}"
    fields:
      - name: id
        type: UUID
        generated: true
      # TODO: Ajouter les champs
'''
        
        # Section API si endpoints trouvés
        if all_endpoints:
            yaml_content += f'''
# ═══════════════════════════════════════════════════════════════════
# API REST
# ═══════════════════════════════════════════════════════════════════

api:
  module: "{list(modules)[0] if modules else 'common'}"
  endpoints:
'''
            for endpoint in all_endpoints[:10]:  # Max 10 endpoints
                parts = endpoint.split()
                method = parts[0] if parts else 'GET'
                path = parts[1] if len(parts) > 1 else '/api/resource'
                yaml_content += f'''    - method: {method}
      path: "{path}"
      description: "TODO"
'''
        
        # Section frontend
        yaml_content += f'''
# ═══════════════════════════════════════════════════════════════════
# FRONTEND
# ═══════════════════════════════════════════════════════════════════

frontend:
  module: "{list(modules)[0] if modules else 'common'}"
  pages:
    - name: "ListPage"
      route: "/{list(modules)[0] if modules else 'module'}"
      type: list
    - name: "DetailPage"
      route: "/{list(modules)[0] if modules else 'module'}/:id"
      type: detail
    - name: "FormPage"
      route: "/{list(modules)[0] if modules else 'module'}/new"
      type: form

# ═══════════════════════════════════════════════════════════════════
# NOTES
# ═══════════════════════════════════════════════════════════════════

notes: |
  Epic E{epic_num} importé depuis OpenProject.
  
  Sprints concernés: {', '.join(sorted(set(t['sprint'] for t in tickets if t['sprint'])))}
  
  Dépendances détectées:
'''
        
        # Ajouter les dépendances
        deps = set()
        for ticket in tickets:
            if ticket['follows']:
                deps.add(ticket['follows'])
            if ticket['parent']:
                deps.add(ticket['parent'])
        
        for dep in sorted(deps)[:5]:
            yaml_content += f"    - {dep}\n"
        
        if not deps:
            yaml_content += "    - Aucune\n"
        
        # Écrire le fichier
        with open(filename, 'w', encoding='utf-8') as f:
            f.write(yaml_content)
        
        print(f"  ✓ E{epic_num}: {feature_name}")
        print(f"      → {len(tickets)} tickets, {total_hours}h, {filename}")
    
    print(f"\n{'='*60}")
    print(f"  ✓ {len(epics)} features générées dans {output_dir}/")
    print(f"{'='*60}\n")

if __name__ == '__main__':
    main()
