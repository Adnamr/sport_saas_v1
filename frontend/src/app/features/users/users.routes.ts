import { Routes } from '@angular/router';

export const USERS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./users-list/users-list.component').then(m => m.UsersListComponent) },
  { path: ':id', loadComponent: () => import('./users-detail/users-detail.component').then(m => m.UsersDetailComponent) }
];
