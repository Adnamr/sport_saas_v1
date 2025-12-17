import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CardComponent } from '@shared/components/card/card.component';

@Component({
  selector: 'app-catalog-list',
  standalone: true,
  imports: [CommonModule, RouterLink, CardComponent],
  template: `
    <div class="page">
      <header class="page-header">
        <h1>Catalogue produits</h1>
        <a routerLink="/dashboard" class="back-link">Retour au dashboard</a>
      </header>
      <app-card>
        <p>Liste des produits - En cours de développement</p>
      </app-card>
    </div>
  `,
  styles: [`
    .page { padding: var(--space-6); max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-6); }
    .page-header h1 { font-size: var(--font-size-2xl); }
    .back-link { color: var(--color-primary); }
  `]
})
export class CatalogListComponent {}
