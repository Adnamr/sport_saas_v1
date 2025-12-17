import { Routes } from '@angular/router';

export const BILLING_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./billing-list/billing-list.component').then(m => m.BillingListComponent) },
  { path: ':id', loadComponent: () => import('./billing-detail/billing-detail.component').then(m => m.BillingDetailComponent) }
];
