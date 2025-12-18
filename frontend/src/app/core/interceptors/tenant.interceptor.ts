import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { environment } from '@env/environment';

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const user = authService.currentUser();

  // Use user's tenant ID if logged in, otherwise use default tenant
  const tenantId = user?.tenantId || environment.defaultTenantId;

  if (tenantId) {
    const cloned = req.clone({
      setHeaders: {
        'X-Tenant-ID': tenantId
      }
    });
    return next(cloned);
  }

  return next(req);
};
