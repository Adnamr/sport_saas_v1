import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

type ButtonVariant = 'primary' | 'secondary' | 'success' | 'danger' | 'ghost';
type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled || loading"
      [class]="buttonClasses"
      (click)="onClick.emit($event)">
      @if (loading) {
        <span class="spinner"></span>
      }
      <ng-content></ng-content>
    </button>
  `,
  styles: [`
    button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: var(--space-2);
      font-weight: var(--font-weight-medium);
      border-radius: var(--radius-md);
      transition: all var(--transition-fast);
      cursor: pointer;

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    .btn-sm { padding: var(--space-1) var(--space-3); font-size: var(--font-size-sm); }
    .btn-md { padding: var(--space-2) var(--space-4); font-size: var(--font-size-base); }
    .btn-lg { padding: var(--space-3) var(--space-6); font-size: var(--font-size-lg); }

    .btn-primary {
      background: var(--color-primary);
      color: var(--color-text-inverse);
      &:hover:not(:disabled) { background: var(--color-primary-hover); }
    }

    .btn-secondary {
      background: var(--color-secondary);
      color: var(--color-text-inverse);
      &:hover:not(:disabled) { background: var(--color-secondary-hover); }
    }

    .btn-success {
      background: var(--color-success);
      color: var(--color-text-inverse);
      &:hover:not(:disabled) { background: var(--color-success-hover); }
    }

    .btn-danger {
      background: var(--color-danger);
      color: var(--color-text-inverse);
      &:hover:not(:disabled) { background: var(--color-danger-hover); }
    }

    .btn-ghost {
      background: transparent;
      color: var(--color-text-primary);
      &:hover:not(:disabled) { background: var(--color-gray-100); }
    }

    .btn-full { width: 100%; }

    .spinner {
      width: 1em;
      height: 1em;
      border: 2px solid currentColor;
      border-right-color: transparent;
      border-radius: 50%;
      animation: spin 0.6s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class ButtonComponent {
  @Input() variant: ButtonVariant = 'primary';
  @Input() size: ButtonSize = 'md';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() disabled = false;
  @Input() loading = false;
  @Input() fullWidth = false;

  @Output() onClick = new EventEmitter<MouseEvent>();

  get buttonClasses(): string {
    return [
      `btn-${this.variant}`,
      `btn-${this.size}`,
      this.fullWidth ? 'btn-full' : ''
    ].filter(Boolean).join(' ');
  }
}
