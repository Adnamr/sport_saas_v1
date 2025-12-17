import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CardComponent } from '@shared/components/card/card.component';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CardComponent],
  template: `
    <div class="dashboard">
      <header class="dashboard-header">
        <div class="header-content">
          <h1>Sport SaaS</h1>
          <nav class="nav">
            <a routerLink="/dashboard" class="nav-link active">Dashboard</a>
            <a routerLink="/catalog" class="nav-link">Catalogue</a>
            <a routerLink="/inventory" class="nav-link">Stock</a>
            <a routerLink="/orders" class="nav-link">Commandes</a>
            <a routerLink="/billing" class="nav-link">Facturation</a>
          </nav>
          <div class="user-menu">
            <span>{{ user()?.firstName }} {{ user()?.lastName }}</span>
            <button (click)="logout()">Déconnexion</button>
          </div>
        </div>
      </header>

      <main class="dashboard-main">
        <h2>Tableau de bord</h2>
        <p class="welcome">Bienvenue, {{ user()?.firstName }} !</p>

        <div class="stats-grid">
          <app-card title="Commandes">
            <div class="stat">
              <span class="stat-value">156</span>
              <span class="stat-label">ce mois</span>
            </div>
          </app-card>

          <app-card title="Chiffre d'affaires">
            <div class="stat">
              <span class="stat-value">12 450 €</span>
              <span class="stat-label">ce mois</span>
            </div>
          </app-card>

          <app-card title="Produits">
            <div class="stat">
              <span class="stat-value">89</span>
              <span class="stat-label">en stock</span>
            </div>
          </app-card>

          <app-card title="Clients">
            <div class="stat">
              <span class="stat-value">234</span>
              <span class="stat-label">actifs</span>
            </div>
          </app-card>
        </div>

        <div class="recent-section">
          <app-card title="Commandes récentes">
            <table class="table">
              <thead>
                <tr>
                  <th>N° Commande</th>
                  <th>Client</th>
                  <th>Montant</th>
                  <th>Statut</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>ORD-001</td>
                  <td>Jean Dupont</td>
                  <td>125.00 €</td>
                  <td><span class="badge badge-success">Confirmée</span></td>
                </tr>
                <tr>
                  <td>ORD-002</td>
                  <td>Marie Martin</td>
                  <td>89.50 €</td>
                  <td><span class="badge badge-warning">En attente</span></td>
                </tr>
              </tbody>
            </table>
          </app-card>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .dashboard { min-height: 100vh; background: var(--color-bg-secondary); }

    .dashboard-header {
      background: var(--color-bg-primary);
      border-bottom: 1px solid var(--color-border);
      padding: var(--space-4);
    }

    .header-content {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--space-6);

      h1 { font-size: var(--font-size-xl); font-weight: var(--font-weight-bold); color: var(--color-primary); margin: 0; }
    }

    .nav { display: flex; gap: var(--space-4); }
    .nav-link {
      color: var(--color-text-secondary);
      font-weight: var(--font-weight-medium);
      padding: var(--space-2);
      border-radius: var(--radius-md);
      &:hover { color: var(--color-primary); background: var(--color-primary-light); }
      &.active { color: var(--color-primary); background: var(--color-primary-light); }
    }

    .user-menu {
      display: flex; align-items: center; gap: var(--space-3);
      span { font-weight: var(--font-weight-medium); }
      button {
        color: var(--color-text-secondary);
        &:hover { color: var(--color-danger); }
      }
    }

    .dashboard-main {
      max-width: 1200px;
      margin: 0 auto;
      padding: var(--space-6);

      h2 { font-size: var(--font-size-2xl); margin-bottom: var(--space-2); }
      .welcome { color: var(--color-text-secondary); margin-bottom: var(--space-6); }
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: var(--space-4);
      margin-bottom: var(--space-6);
    }

    .stat { text-align: center; padding: var(--space-4) 0; }
    .stat-value { display: block; font-size: var(--font-size-3xl); font-weight: var(--font-weight-bold); color: var(--color-primary); }
    .stat-label { color: var(--color-text-secondary); font-size: var(--font-size-sm); }

    .table { width: 100%; }
    .table th, .table td { padding: var(--space-3); text-align: left; border-bottom: 1px solid var(--color-border); }
    .table th { font-weight: var(--font-weight-semibold); color: var(--color-text-secondary); font-size: var(--font-size-sm); }

    .badge { display: inline-block; padding: var(--space-1) var(--space-2); border-radius: var(--radius-full); font-size: var(--font-size-xs); font-weight: var(--font-weight-medium); }
    .badge-success { background: var(--color-success-light); color: var(--color-success); }
    .badge-warning { background: var(--color-warning-light); color: var(--color-warning); }
  `]
})
export class DashboardComponent {
  private authService = inject(AuthService);
  user = this.authService.currentUser;

  logout(): void {
    this.authService.logout();
  }
}
