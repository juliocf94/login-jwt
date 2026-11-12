import { Routes } from '@angular/router';
import { authGuard, invitadoGuard } from './core/auth.guard';
import { Login } from './login/login';
import { Contribuyentes } from './contribuyentes/contribuyentes';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [invitadoGuard] },
  { path: 'contribuyentes', component: Contribuyentes, canActivate: [authGuard] },  // requiere sesión
  { path: '', redirectTo: 'contribuyentes', pathMatch: 'full' },
  { path: '**', redirectTo: 'contribuyentes' },
];
