import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-users-detail',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page"><h1>Détail utilisateur</h1></div>`,
  styles: [`.page { padding: var(--space-6); }`]
})
export class UsersDetailComponent {}
