import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Une erreur est survenue';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = error.error.message;
      } else {
        // Server-side error
        switch (error.status) {
          case 401:
            errorMessage = 'Session expirée. Veuillez vous reconnecter.';
            router.navigate(['/auth/login']);
            break;
          case 403:
            errorMessage = 'Accès refusé.';
            break;
          case 404:
            errorMessage = 'Ressource non trouvée.';
            break;
          case 422:
            errorMessage = error.error?.message || 'Données invalides.';
            break;
          case 500:
            errorMessage = 'Erreur serveur. Veuillez réessayer.';
            break;
          default:
            errorMessage = error.error?.message || errorMessage;
        }
      }

      console.error('HTTP Error:', error);
      return throwError(() => ({ ...error, message: errorMessage }));
    })
  );
};
