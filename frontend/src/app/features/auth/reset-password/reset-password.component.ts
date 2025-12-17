import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CardComponent } from '@shared/components/card/card.component';
import { ButtonComponent } from '@shared/components/button/button.component';
import { InputComponent } from '@shared/components/input/input.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, RouterLink, CardComponent, ButtonComponent, InputComponent, ReactiveFormsModule],
  template: `
    <div class="auth-container">
      <app-card class="auth-card">
        <div class="auth-header">
          <h1>Nouveau mot de passe</h1>
          <p>Choisissez un nouveau mot de passe</p>
        </div>
        <form [formGroup]="form">
          <div class="form-group">
            <app-input label="Nouveau mot de passe" type="password" formControlName="password" [required]="true"></app-input>
          </div>
          <div class="form-group">
            <app-input label="Confirmer le mot de passe" type="password" formControlName="confirmPassword" [required]="true"></app-input>
          </div>
          <app-button type="submit" [fullWidth]="true">Réinitialiser</app-button>
        </form>
      </app-card>
    </div>
  `,
  styles: [`
    .auth-container { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: var(--space-4); background: var(--color-bg-secondary); }
    .auth-card { width: 100%; max-width: 400px; }
    .auth-header { text-align: center; margin-bottom: var(--space-6); h1 { font-size: var(--font-size-2xl); font-weight: var(--font-weight-bold); margin-bottom: var(--space-2); } p { color: var(--color-text-secondary); } }
    .form-group { margin-bottom: var(--space-4); }
  `]
})
export class ResetPasswordComponent {
  form = new FormBuilder().group({
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  });
}
