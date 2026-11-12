import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';

export interface Contribuyente {
  id: number;
  nit: string;
  nombre: string;
  email: string | null;
  tipo: 'PERSONA' | 'EMPRESA';
}

/** Lo que se envía en POST / PUT (sin id) */
export type ContribuyenteRequest = Omit<Contribuyente, 'id'>;

/**
 * Llamadas HTTP del CRUD. El token NO se agrega aquí:
 * lo agrega auth.interceptor.ts automáticamente.
 */
@Injectable({ providedIn: 'root' })
export class ContribuyenteService {
  private http = inject(HttpClient);
  private url = `${API_URL}/api/contribuyentes`;

  listar(): Observable<Contribuyente[]> {
    return this.http.get<Contribuyente[]>(this.url);
  }

  crear(data: ContribuyenteRequest): Observable<Contribuyente> {
    return this.http.post<Contribuyente>(this.url, data);
  }

  actualizar(id: number, data: ContribuyenteRequest): Observable<Contribuyente> {
    return this.http.put<Contribuyente>(`${this.url}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
