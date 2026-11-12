import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { mensajeError } from '../core/mensaje-error';

/**
 * Pantalla de login.
 * Validaciones del lado del cliente (el backend vuelve a validar igual):
 *   - usuario obligatorio, 3 a 50 caracteres, sin espacios
 *   - contraseña obligatoria, 6 a 100 caracteres
 *   - botón deshabilitado si el form es inválido o mientras se espera respuesta
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly cargando = signal(false);
  readonly error = signal('');
  readonly verClave = signal(false);

  readonly form = inject(FormBuilder).nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(/^\S+$/)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
  });

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();   // muestra los errores de todos los campos
      return;
    }
    this.cargando.set(true);
    this.error.set('');

    const { username, password } = this.form.getRawValue();
    this.auth.login(username.trim(), password).subscribe({
      next: () => this.router.navigate(['/contribuyentes']),
      error: (err) => {
        this.error.set(mensajeError(err));          // 401 -> "Usuario o contraseña incorrectos"
        this.form.controls.password.reset();        // limpiar la clave tras un intento fallido
        this.cargando.set(false);
      },
    });
  }

  /** ¿Mostrar el error de este campo? Solo si ya lo tocó. */
  invalido(campo: 'username' | 'password'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }
}
