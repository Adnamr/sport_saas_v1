import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { ButtonComponent } from '@shared/components/button/button.component';
import { InputComponent } from '@shared/components/input/input.component';
import { CardComponent } from '@shared/components/card/card.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ButtonComponent, InputComponent, CardComponent],
  template: `
    <div class="auth-container">
      <app-card class="auth-card">
        <div class="auth-header">
          <h1>Créer un compte</h1>
          <p>Rejoignez Sport SaaS</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          @if (errorMessage) {
            <div class="alert alert-error">{{ errorMessage }}</div>
          }

          <div class="form-row">
            <div class="form-group">
              <app-input label="Prénom" formControlName="firstName" [error]="getError('firstName')" [required]="true"></app-input>
            </div>
            <div class="form-group">
              <app-input label="Nom" formControlName="lastName" [error]="getError('lastName')" [required]="true"></app-input>
            </div>
          </div>

          <div class="form-group">
            <app-input label="Email" type="email" formControlName="email" [error]="getError('email')" [required]="true"></app-input>
          </div>

          <div class="form-group">
            <app-input label="Mot de passe" type="password" formControlName="password" [error]="getError('password')" [required]="true"></app-input>
          </div>

          <app-button type="submit" [fullWidth]="true" [loading]="isLoading" [disabled]="form.invalid">
            Créer mon compte
          </app-button>

          <p class="login-link">
            Déjà un compte ? <a routerLink="/auth/login">Se connecter</a>
          </p>
        </form>
      </app-card>
    </div>
  `,
  styles: [`
    .auth-container { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: var(--space-4); background: var(--color-bg-secondary); }
    .auth-card { width: 100%; max-width: 450px; }
    .auth-header { text-align: center; margin-bottom: var(--space-6); h1 { font-size: var(--font-size-2xl); font-weight: var(--font-weight-bold); margin-bottom: var(--space-2); } p { color: var(--color-text-secondary); } }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }
    .form-group { margin-bottom: var(--space-4); }
    .login-link { text-align: center; margin-top: var(--space-4); font-size: var(--font-size-sm); color: var(--color-text-secondary); a { color: var(--color-primary); &:hover { text-decoration: underline; } } }
    .alert { padding: var(--space-3); border-radius: var(--radius-md); margin-bottom: var(--space-4); }
    .alert-error { background: var(--color-danger-light); color: var(--color-danger); border: 1px solid var(--color-danger); }
  `]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  isLoading = false;
  errorMessage = '';

  onSubmit(): void {
    if (this.form.invalid) return;
    this.isLoading = true;
    this.authService.register(this.form.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (error) => { this.isLoading = false; this.errorMessage = error.message; }
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
