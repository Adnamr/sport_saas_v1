import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `<div class="page"><h1>Gestion utilisateurs</h1><a routerLink="/dashboard">Retour</a></div>`,
  styles: [`.page { padding: var(--space-6); } h1 { margin-bottom: var(--space-4); }`]
})
export class UsersListComponent {}
