import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '@env/environment';

export interface TrendValue {
  value: number;
  trend: number;
  period: string;
}

export interface DashboardStats {
  revenue: TrendValue;
  orders: TrendValue;
  products: TrendValue;
  customers: TrendValue;
}

export interface RevenueChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
  }[];
}

export interface OrdersByStatus {
  labels: string[];
  data: number[];
  colors: string[];
}

export interface RecentOrder {
  id: string;
  orderNumber: string;
  customerName: string;
  customerAvatar?: string;
  amount: number;
  status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled';
  date: string;
}

export interface LowStockItem {
  id: string;
  name: string;
  sku: string;
  currentStock: number;
  minStock: number;
  category: string;
}

export interface ActivityItem {
  id: string;
  type: 'order' | 'payment' | 'stock' | 'user' | 'product';
  message: string;
  timestamp: string;
  icon: string;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly apiUrl = `${environment.apiUrl}/api/dashboard`;
  private http = inject(HttpClient);

  getStats(): Observable<DashboardStats> {
    // TODO: Replace with actual API call
    // return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
    return of({
      revenue: { value: 45230, trend: 12.5, period: 'vs mois dernier' },
      orders: { value: 127, trend: 8.2, period: 'vs mois dernier' },
      products: { value: 543, trend: -2.1, period: 'vs mois dernier' },
      customers: { value: 89, trend: 15.3, period: 'vs mois dernier' }
    });
  }

  getRevenueChart(period: 'week' | 'month' | 'year' = 'month'): Observable<RevenueChartData> {
    // TODO: Replace with actual API call
    // return this.http.get<RevenueChartData>(`${this.apiUrl}/revenue-chart`, { params: { period } });
    return of({
      labels: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc'],
      datasets: [
        {
          label: 'CA 2024',
          data: [12000, 19000, 15000, 25000, 22000, 30000, 35000, 28000, 40000, 38000, 42000, 45230]
        },
        {
          label: 'CA 2023',
          data: [10000, 15000, 12000, 20000, 18000, 25000, 28000, 24000, 32000, 30000, 35000, 38000]
        }
      ]
    });
  }

  getOrdersByStatus(): Observable<OrdersByStatus> {
    // TODO: Replace with actual API call
    // return this.http.get<OrdersByStatus>(`${this.apiUrl}/orders-by-status`);
    return of({
      labels: ['En attente', 'En cours', 'Expédiées', 'Livrées', 'Annulées'],
      data: [15, 23, 35, 48, 6],
      colors: ['#f59e0b', '#3b82f6', '#8b5cf6', '#10b981', '#ef4444']
    });
  }

  getRecentOrders(limit: number = 5): Observable<RecentOrder[]> {
    // TODO: Replace with actual API call
    // return this.http.get<RecentOrder[]>(`${this.apiUrl}/recent-orders`, { params: { limit } });
    return of([
      { id: '1', orderNumber: 'CMD-2024-0127', customerName: 'Jean Dupont', amount: 299.99, status: 'processing', date: '2024-12-18T10:30:00' },
      { id: '2', orderNumber: 'CMD-2024-0126', customerName: 'Marie Martin', amount: 549.50, status: 'shipped', date: '2024-12-18T09:15:00' },
      { id: '3', orderNumber: 'CMD-2024-0125', customerName: 'Pierre Durant', amount: 89.00, status: 'delivered', date: '2024-12-17T16:45:00' },
      { id: '4', orderNumber: 'CMD-2024-0124', customerName: 'Sophie Leroy', amount: 1250.00, status: 'pending', date: '2024-12-17T14:20:00' },
      { id: '5', orderNumber: 'CMD-2024-0123', customerName: 'Lucas Bernard', amount: 175.00, status: 'delivered', date: '2024-12-17T11:00:00' }
    ]);
  }

  getLowStockItems(limit: number = 5): Observable<LowStockItem[]> {
    // TODO: Replace with actual API call
    // return this.http.get<LowStockItem[]>(`${this.apiUrl}/low-stock`, { params: { limit } });
    return of([
      { id: '1', name: 'Ballon Football Pro', sku: 'BFP-001', currentStock: 3, minStock: 10, category: 'Football' },
      { id: '2', name: 'Raquette Tennis Elite', sku: 'RTE-015', currentStock: 5, minStock: 15, category: 'Tennis' },
      { id: '3', name: 'Chaussures Running X9', sku: 'CRX-042', currentStock: 2, minStock: 20, category: 'Running' },
      { id: '4', name: 'Maillot Cyclisme Pro', sku: 'MCP-008', currentStock: 8, minStock: 12, category: 'Cyclisme' },
      { id: '5', name: 'Gants Boxe Premium', sku: 'GBP-003', currentStock: 4, minStock: 10, category: 'Boxe' }
    ]);
  }

  getActivityFeed(limit: number = 10): Observable<ActivityItem[]> {
    // TODO: Replace with actual API call
    // return this.http.get<ActivityItem[]>(`${this.apiUrl}/activity`, { params: { limit } });
    return of([
      { id: '1', type: 'order', message: 'Nouvelle commande CMD-2024-0127 de Jean Dupont', timestamp: '2024-12-18T10:30:00', icon: '🛒' },
      { id: '2', type: 'payment', message: 'Paiement reçu pour CMD-2024-0125 (89,00 €)', timestamp: '2024-12-18T10:15:00', icon: '💳' },
      { id: '3', type: 'stock', message: 'Stock faible: Ballon Football Pro (3 unités)', timestamp: '2024-12-18T09:45:00', icon: '📦' },
      { id: '4', type: 'user', message: 'Nouveau client inscrit: Sophie Leroy', timestamp: '2024-12-18T09:30:00', icon: '👤' },
      { id: '5', type: 'product', message: 'Produit mis à jour: Raquette Tennis Elite', timestamp: '2024-12-18T09:00:00', icon: '📝' },
      { id: '6', type: 'order', message: 'Commande CMD-2024-0126 expédiée', timestamp: '2024-12-17T17:00:00', icon: '🚚' },
      { id: '7', type: 'stock', message: 'Réapprovisionnement: +50 Maillots Cyclisme', timestamp: '2024-12-17T15:30:00', icon: '📥' }
    ]);
  }
}