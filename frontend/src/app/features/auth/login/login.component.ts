import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { ButtonComponent } from '@shared/components/button/button.component';
import { InputComponent } from '@shared/components/input/input.component';
import { CardComponent } from '@shared/components/card/card.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    ButtonComponent,
    InputComponent,
    CardComponent
  ],
  template: `
    <div class="auth-container">
      <app-card class="auth-card">
        <div class="auth-header">
          <h1>Connexion</h1>
          <p>Accédez à votre espace Sport SaaS</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          @if (errorMessage) {
            <div class="alert alert-error">{{ errorMessage }}</div>
          }

          <div class="form-group">
            <app-input
              label="Email"
              type="email"
              formControlName="email"
              placeholder="votre@email.com"
              [error]="getError('email')"
              [required]="true">
            </app-input>
          </div>

          <div class="form-group">
            <app-input
              label="Mot de passe"
              type="password"
              formControlName="password"
              placeholder="Votre mot de passe"
              [error]="getError('password')"
              [required]="true">
            </app-input>
          </div>

          <div class="form-actions">
            <a routerLink="/auth/forgot-password" class="forgot-link">
              Mot de passe oublié ?
            </a>
          </div>

          <app-button
            type="submit"
            [fullWidth]="true"
            [loading]="isLoading"
            [disabled]="form.invalid">
            Se connecter
          </app-button>

          <p class="register-link">
            Pas encore de compte ?
            <a routerLink="/auth/register">Créer un compte</a>
          </p>
        </form>
      </app-card>
    </div>
  `,
  styles: [`
    .auth-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: var(--space-4);
      background: var(--color-bg-secondary);
    }

    .auth-card {
      width: 100%;
      max-width: 400px;
    }

    .auth-header {
      text-align: center;
      margin-bottom: var(--space-6);

      h1 {
        font-size: var(--font-size-2xl);
        font-weight: var(--font-weight-bold);
        color: var(--color-text-primary);
        margin-bottom: var(--space-2);
      }

      p {
        color: var(--color-text-secondary);
      }
    }

    .form-group {
      margin-bottom: var(--space-4);
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      margin-bottom: var(--space-4);
    }

    .forgot-link {
      font-size: var(--font-size-sm);
      color: var(--color-primary);
      &:hover { text-decoration: underline; }
    }

    .register-link {
      text-align: center;
      margin-top: var(--space-4);
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);

      a {
        color: var(--color-primary);
        font-weight: var(--font-weight-medium);
        &:hover { text-decoration: underline; }
      }
    }

    .alert {
      padding: var(--space-3);
      border-radius: var(--radius-md);
      margin-bottom: var(--space-4);
      font-size: var(--font-size-sm);
    }

    .alert-error {
      background: var(--color-danger-light);
      color: var(--color-danger);
      border: 1px solid var(--color-danger);
    }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  isLoading = false;
  errorMessage = '';

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.message || 'Identifiants incorrects';
      }
    });
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.touched && control?.errors) {
      if (control.errors['required']) return 'Ce champ est requis';
      if (control.errors['email']) return 'Email invalide';
      if (control.errors['minlength']) return 'Minimum 8 caractères';
    }
    return '';
  }
}
