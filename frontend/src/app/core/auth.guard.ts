import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/** Protege rutas privadas: sin sesión -> /login */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.logueado() ? true : inject(Router).createUrlTree(['/login']);
};

/** Evita ver el login si ya hay sesión -> /contribuyentes */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.logueado() ? inject(Router).createUrlTree(['/contribuyentes']) : true;
};
