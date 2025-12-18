import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    organization: ['', Validators.required],
    organizationType: ['club'],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required],
    acceptCgu: [false, Validators.requiredTrue],
    newsletter: [false]
  }, { validators: this.passwordMatchValidator });

  isLoading = false;
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

  organizationTypes = [
    { value: 'club', label: 'Club sportif' },
    { value: 'association', label: 'Association' },
    { value: 'retailer', label: 'Revendeur' },
    { value: 'school', label: 'École / Université' }
  ];

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
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
    if (this.form.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    const formValue = this.form.value;
    this.authService.register({
      firstName: formValue.firstName!,
      lastName: formValue.lastName!,
      email: formValue.email!,
      password: formValue.password!
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/auth/login'], {
          queryParams: { registered: true }
        });
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Une erreur est survenue';
      }
    });
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.touched && control?.errors) {
      if (control.errors['required']) return 'Ce champ est requis';
      if (control.errors['requiredTrue']) return 'Vous devez accepter les CGU';
      if (control.errors['email']) return 'Email invalide';
      if (control.errors['minlength']) return `Minimum ${control.errors['minlength'].requiredLength} caractères`;
      if (control.errors['passwordMismatch']) return 'Les mots de passe ne correspondent pas';
    }
    return '';
  }
}