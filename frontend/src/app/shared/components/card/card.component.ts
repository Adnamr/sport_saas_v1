import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card" [class.hoverable]="hoverable">
      @if (title || headerTemplate) {
        <div class="card-header">
          @if (title) {
            <h3 class="card-title">{{ title }}</h3>
          }
          <ng-content select="[card-header]"></ng-content>
        </div>
      }
      <div class="card-body" [class.no-padding]="noPadding">
        <ng-content></ng-content>
      </div>
      @if (footerTemplate) {
        <div class="card-footer">
          <ng-content select="[card-footer]"></ng-content>
        </div>
      }
    </div>
  `,
  styles: [`
    .card {
      background: var(--color-bg-primary);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-lg);
      overflow: hidden;

      &.hoverable {
        transition: box-shadow var(--transition-fast);
        &:hover { box-shadow: var(--shadow-md); }
      }
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: var(--space-4);
      border-bottom: 1px solid var(--color-border);
    }

    .card-title {
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
      color: var(--color-text-primary);
      margin: 0;
    }

    .card-body {
      padding: var(--space-4);
      &.no-padding { padding: 0; }
    }

    .card-footer {
      padding: var(--space-4);
      border-top: 1px solid var(--color-border);
      background: var(--color-bg-secondary);
    }
  `]
})
export class CardComponent {
  @Input() title = '';
  @Input() hoverable = false;
  @Input() noPadding = false;
  @Input() headerTemplate = false;
  @Input() footerTemplate = false;
}
