import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inventory-detail',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page"><h1>Détail stock</h1></div>`,
  styles: [`.page { padding: var(--space-6); }`]
})
export class InventoryDetailComponent {}
