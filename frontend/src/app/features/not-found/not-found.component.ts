import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ButtonComponent } from '@shared/components/button/button.component';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonComponent],
  template: `
    <div class="not-found-container">
      <div class="not-found-content">
        <h1 class="error-code">404</h1>
        <h2 class="error-title">Page introuvable</h2>
        <p class="error-message">
          La page que vous recherchez n'existe pas ou a été déplacée.
        </p>
        <app-button routerLink="/dashboard">
          Retour à l'accueil
        </app-button>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: var(--space-4);
      background: var(--color-bg-secondary);
    }

    .not-found-content {
      text-align: center;
      max-width: 400px;
    }

    .error-code {
      font-size: 8rem;
      font-weight: var(--font-weight-bold);
      color: var(--color-primary);
      line-height: 1;
      margin-bottom: var(--space-4);
    }

    .error-title {
      font-size: var(--font-size-2xl);
      font-weight: var(--font-weight-semibold);
      color: var(--color-text-primary);
      margin-bottom: var(--space-2);
    }

    .error-message {
      color: var(--color-text-secondary);
      margin-bottom: var(--space-6);
    }
  `]
})
export class NotFoundComponent {}
