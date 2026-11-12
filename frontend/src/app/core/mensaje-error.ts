import { HttpErrorResponse } from '@angular/common/http';

/**
 * Convierte cualquier error HTTP en un texto para mostrar.
 * Entiende el formato de error de los DOS backends:
 *   Spring  400 -> { mensaje, errores: ["campo: msg"] }
 *   Quarkus 400 -> { title, violations: [{ field, message }] }
 *   Ambos 404/409/401 -> { status, mensaje }
 */
export function mensajeError(err: HttpErrorResponse): string {
  if (err.status === 0) return 'No se pudo conectar con el servidor (¿backend levantado? ¿CORS? ¿URL en api.config.ts?)';
  if (err.status === 403) return 'No tienes permiso para esta acción';

  const body = err.error ?? {};
  if (Array.isArray(body.errores)) return body.errores.join(' · ');
  if (Array.isArray(body.violations)) return body.violations.map((v: { message: string }) => v.message).join(' · ');
  return body.mensaje ?? `Error ${err.status}`;
}
