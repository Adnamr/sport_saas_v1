import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { AuthService } from '@core/services/auth.service';
import {
  DashboardService,
  DashboardStats,
  RecentOrder,
  LowStockItem,
  ActivityItem
} from '@core/services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NgChartsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private dashboardService = inject(DashboardService);

  user = this.authService.currentUser;
  isLoading = signal(true);

  stats = signal<DashboardStats | null>(null);
  recentOrders = signal<RecentOrder[]>([]);
  lowStockItems = signal<LowStockItem[]>([]);
  activityFeed = signal<ActivityItem[]>([]);

  // Revenue Chart
  revenueChartData: ChartData<'line'> = {
    labels: [],
    datasets: []
  };

  revenueChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          usePointStyle: true,
          padding: 20
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value) => value.toLocaleString('fr-FR') + ' €'
        }
      }
    },
    elements: {
      line: {
        tension: 0.4
      }
    }
  };

  // Orders Status Chart
  ordersChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: []
  };

  ordersChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'right',
        labels: {
          usePointStyle: true,
          padding: 15
        }
      }
    },
    cutout: '70%'
  };

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.isLoading.set(true);

    // Load stats
    this.dashboardService.getStats().subscribe({
      next: (data) => this.stats.set(data)
    });

    // Load revenue chart
    this.dashboardService.getRevenueChart().subscribe({
      next: (data) => {
        this.revenueChartData = {
          labels: data.labels,
          datasets: data.datasets.map((ds, i) => ({
            ...ds,
            borderColor: i === 0 ? '#3b82f6' : '#94a3b8',
            backgroundColor: i === 0 ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
            fill: i === 0,
            pointRadius: 4,
            pointHoverRadius: 6
          }))
        };
      }
    });

    // Load orders by status
    this.dashboardService.getOrdersByStatus().subscribe({
      next: (data) => {
        this.ordersChartData = {
          labels: data.labels,
          datasets: [{
            data: data.data,
            backgroundColor: data.colors,
            borderWidth: 0,
            hoverOffset: 10
          }]
        };
      }
    });

    // Load recent orders
    this.dashboardService.getRecentOrders(5).subscribe({
      next: (data) => this.recentOrders.set(data)
    });

    // Load low stock items
    this.dashboardService.getLowStockItems(5).subscribe({
      next: (data) => this.lowStockItems.set(data)
    });

    // Load activity feed
    this.dashboardService.getActivityFeed(7).subscribe({
      next: (data) => {
        this.activityFeed.set(data);
        this.isLoading.set(false);
      }
    });
  }

  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      pending: 'bg-amber-100 text-amber-700',
      processing: 'bg-blue-100 text-blue-700',
      shipped: 'bg-purple-100 text-purple-700',
      delivered: 'bg-green-100 text-green-700',
      cancelled: 'bg-red-100 text-red-700'
    };
    return classes[status] || 'bg-gray-100 text-gray-700';
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      pending: 'En attente',
      processing: 'En cours',
      shipped: 'Expédiée',
      delivered: 'Livrée',
      cancelled: 'Annulée'
    };
    return labels[status] || status;
  }

  getStockLevel(current: number, min: number): string {
    const ratio = current / min;
    if (ratio <= 0.2) return 'bg-red-500';
    if (ratio <= 0.5) return 'bg-amber-500';
    return 'bg-green-500';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);

    if (minutes < 1) return "À l'instant";
    if (minutes < 60) return `Il y a ${minutes} min`;
    if (hours < 24) return `Il y a ${hours}h`;
    return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
  }

  logout(): void {
    this.authService.logout();
  }
}