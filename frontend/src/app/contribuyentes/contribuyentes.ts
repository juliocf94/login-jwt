import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../core/auth.service';
import { mensajeError } from '../core/mensaje-error';
import { Contribuyente, ContribuyenteService } from './contribuyente.service';

/**
 * Pantalla principal después del login.
 *   VIEW  -> solo ve la tabla
 *   ADMIN -> además ve el formulario y los botones Editar / Eliminar
 *
 * Ocultar botones es solo comodidad visual: la seguridad real está en el backend
 * (si un VIEW intenta un POST, el backend responde 403).
 */
@Component({
  selector: 'app-contribuyentes',
  imports: [ReactiveFormsModule],
  templateUrl: './contribuyentes.html',
})
export class Contribuyentes implements OnInit {
  readonly auth = inject(AuthService);
  private api = inject(ContribuyenteService);

  readonly lista = signal<Contribuyente[]>([]);
  readonly editandoId = signal<number | null>(null);   // null = creando
  readonly error = signal('');
  readonly mensaje = signal('');

  // Mismas reglas que ContribuyenteRequest en el backend
  readonly form = inject(FormBuilder).nonNullable.group({
    nit: ['', [Validators.required, Validators.maxLength(20)]],
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    tipo: ['PERSONA' as 'PERSONA' | 'EMPRESA', Validators.required],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.api.listar().subscribe({
      next: (datos) => this.lista.set(datos),
      error: (err) => this.error.set(mensajeError(err)),
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const data = { ...v, email: v.email || null };       // "" -> null
    const id = this.editandoId();
    const peticion = id === null ? this.api.crear(data) : this.api.actualizar(id, data);

    peticion.subscribe({
      next: () => {
        this.aviso(id === null ? 'Contribuyente creado' : 'Contribuyente actualizado');
        this.cancelar();
        this.cargar();
      },
      error: (err) => this.error.set(mensajeError(err)),   // 400, 403, 409...
    });
  }

  editar(c: Contribuyente): void {
    this.editandoId.set(c.id);
    this.form.setValue({ nit: c.nit, nombre: c.nombre, email: c.email ?? '', tipo: c.tipo });
  }

  eliminar(c: Contribuyente): void {
    if (!confirm(`¿Eliminar a ${c.nombre}?`)) return;
    this.api.eliminar(c.id).subscribe({
      next: () => { this.aviso('Contribuyente eliminado'); this.cargar(); },
      error: (err) => this.error.set(mensajeError(err)),
    });
  }

  cancelar(): void {
    this.editandoId.set(null);
    this.form.reset();
  }

  invalido(campo: 'nit' | 'nombre' | 'email'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }

  private aviso(texto: string): void {
    this.error.set('');
    this.mensaje.set(texto);
    setTimeout(() => this.mensaje.set(''), 3000);
  }
}
