import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const user = authService.currentUser();

  if (user?.tenantId) {
    const cloned = req.clone({
      setHeaders: {
        'X-Tenant-ID': user.tenantId
      }
    });
    return next(cloned);
  }

  return next(req);
};
