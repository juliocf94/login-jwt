import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../api.config';

/** Lo que responde POST /api/auth/login (igual en Quarkus y Spring) */
export interface LoginResponse {
  token: string;
  username: string;
  rol: 'ADMIN' | 'VIEW';
  expiraEnSegundos: number;
}

const CLAVE = 'sesion';   // clave en localStorage

/**
 * Maneja la sesión: login, logout, guardar el token y saber el rol.
 * providedIn 'root' = una sola instancia para toda la app (singleton).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  /** Sesión actual (null = no logueado). Se recupera de localStorage al recargar. */
  readonly sesion = signal<LoginResponse | null>(this.leerSesionGuardada());

  readonly logueado = computed(() => this.sesion() !== null);
  readonly esAdmin = computed(() => this.sesion()?.rol === 'ADMIN');

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_URL}/api/auth/login`, { username, password })
      .pipe(tap((r) => {
        localStorage.setItem(CLAVE, JSON.stringify(r));
        this.sesion.set(r);
      }));
  }

  logout(): void {
    localStorage.removeItem(CLAVE);
    this.sesion.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    return this.sesion()?.token ?? null;
  }

  /** Lee la sesión guardada y la descarta si el token ya expiró. */
  private leerSesionGuardada(): LoginResponse | null {
    const raw = localStorage.getItem(CLAVE);
    if (!raw) return null;
    try {
      const s = JSON.parse(raw) as LoginResponse;
      return this.tokenExpirado(s.token) ? (localStorage.removeItem(CLAVE), null) : s;
    } catch {
      return null;
    }
  }

  /**
   * Un JWT son 3 partes separadas por punto: header.payload.firma
   * El payload es JSON en Base64URL; "exp" = fecha de expiración en segundos.
   * (Angular solo LEE el token; quien lo valida de verdad es el backend.)
   */
  private tokenExpirado(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return Date.now() >= payload.exp * 1000;
    } catch {
      return true;
    }
  }
}
