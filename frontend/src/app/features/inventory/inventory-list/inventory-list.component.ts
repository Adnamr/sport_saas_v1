import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `<div class="page"><h1>Gestion du stock</h1><a routerLink="/dashboard">Retour</a></div>`,
  styles: [`.page { padding: var(--space-6); } h1 { margin-bottom: var(--space-4); }`]
})
export class InventoryListComponent {}
