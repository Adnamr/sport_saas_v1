import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { AuthService } from '@core/services/auth.service';

type TokenState = 'validating' | 'valid' | 'invalid' | 'expired';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);

  form = this.fb.group({
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordMatchValidator });

  token = '';
  tokenState = signal<TokenState>('validating');
  isLoading = false;
  isSuccess = false;
  errorMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  passwordStrength = signal<'none' | 'weak' | 'medium' | 'strong'>('none');

  strengthLabel = computed(() => {
    const strength = this.passwordStrength();
    switch (strength) {
      case 'weak': return 'Faible';
      case 'medium': return 'Moyen';
      case 'strong': return 'Fort';
      default: return '';
    }
  });

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';

    if (!this.token) {
      this.tokenState.set('invalid');
    } else {
      this.validateToken();
    }
  }

  private validateToken(): void {
    this.tokenState.set('validating');

    this.authService.validateResetToken(this.token).subscribe({
      next: (response) => {
        if (response.valid) {
          this.tokenState.set('valid');
        } else if (response.expired) {
          this.tokenState.set('expired');
        } else {
          this.tokenState.set('invalid');
        }
      },
      error: () => {
        this.tokenState.set('invalid');
      }
    });
  }

  onPasswordChange(): void {
    const password = this.form.get('password')?.value || '';
    this.passwordStrength.set(this.calculateStrength(password));
  }

  calculateStrength(password: string): 'none' | 'weak' | 'medium' | 'strong' {
    if (!password) return 'none';
    let score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    if (score <= 2) return 'weak';
    if (score <= 4) return 'medium';
    return 'strong';
  }

  onSubmit(): void {
    if (this.form.invalid || !this.token || this.tokenState() !== 'valid') return;

    this.isLoading = true;
    this.errorMessage = '';

    const password = this.form.get('password')?.value as string;

    this.authService.resetPassword(this.token, password).subscribe({
      next: () => {
        this.isLoading = false;
        this.isSuccess = true;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 410) {
          this.tokenState.set('expired');
        } else {
          this.errorMessage = error.message || 'Une erreur est survenue';
        }
      }
    });
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.touched && control?.errors) {
      if (control.errors['required']) return 'Ce champ est requis';
      if (control.errors['minlength']) return 'Minimum 8 caractères';
    }

    if (field === 'confirmPassword' && this.form.errors?.['passwordMismatch']) {
      const confirmControl = this.form.get('confirmPassword');
      if (confirmControl?.touched) {
        return 'Les mots de passe ne correspondent pas';
      }
    }

    return '';
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }
}