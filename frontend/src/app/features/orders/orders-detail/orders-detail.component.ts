import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-orders-detail',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page"><h1>Détail commande</h1></div>`,
  styles: [`.page { padding: var(--space-6); }`]
})
export class OrdersDetailComponent {}
