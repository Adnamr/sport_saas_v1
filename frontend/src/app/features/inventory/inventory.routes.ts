import { Routes } from '@angular/router';

export const INVENTORY_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./inventory-list/inventory-list.component').then(m => m.InventoryListComponent) },
  { path: ':id', loadComponent: () => import('./inventory-detail/inventory-detail.component').then(m => m.InventoryDetailComponent) }
];
