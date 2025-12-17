import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CardComponent } from '@shared/components/card/card.component';
import { AuthService } from '@core/services/auth.service';

interface DashboardStats {
  ordersCount: number;
  revenue: number;
  productsCount: number;
  customersCount: number;
}

interface RecentOrder {
  id: string;
  orderNumber: string;
  customerName: string;
  amount: number;
  statusLabel: string;
  statusClass: 'success' | 'warning' | 'danger';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CardComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  private authService = inject(AuthService);
  user = this.authService.currentUser;

  // TODO: Replace with actual API calls
  stats: DashboardStats = {
    ordersCount: 0,
    revenue: 0,
    productsCount: 0,
    customersCount: 0
  };

  recentOrders: RecentOrder[] = [];

  logout(): void {
    this.authService.logout();
  }
}
