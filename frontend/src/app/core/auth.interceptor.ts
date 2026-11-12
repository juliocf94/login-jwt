import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Se ejecuta en CADA petición HTTP (como un middleware de Laravel/Express, pero del lado cliente).
 *   1. Si hay token, agrega:  Authorization: Bearer <token>
 *   2. Si el backend responde 401 (token vencido o inválido), cierra la sesión.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();

  const peticion = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((err) => {
      // En el login un 401 significa "clave incorrecta": eso lo maneja la pantalla de login
      if (err.status === 401 && !req.url.includes('/api/auth/login')) {
        auth.logout();
      }
      return throwError(() => err);
    }),
  );
};
