# Login JWT + CRUD por roles · Angular + (Quarkus **o** Spring Boot) + Oracle

Molde funcional para la prueba técnica: **login con JWT**, dos roles (**ADMIN** y **VIEW**) y un **CRUD** de contribuyentes sobre **Oracle en Docker**.

Hay **dos backends que hacen exactamente lo mismo** (mismas URLs, mismos JSON, mismos códigos HTTP). Usas el que pidan en la prueba; Angular funciona con cualquiera de los dos.

```
login-jwt/
├─ docker-compose.yml        Oracle 23 Free (crea usuario app/app123 y ejecuta db/*.sql)
├─ db/01_schema.sql          Tablas USUARIO y CONTRIBUYENTE + datos de prueba
├─ backend-quarkus/          Puerto 8081  ·  Swagger: /q/swagger-ui
├─ backend-spring/           Puerto 8080  ·  Swagger: /swagger-ui.html
├─ frontend/                 Angular 22   ·  http://localhost:4200
├─ postman/                  Colección lista para importar
└─ docs/
   ├─ PASOS-PRUEBA.md        Comandos paso a paso para el día de la prueba  ← EMPIEZA AQUÍ (también en Pasos-Prueba.pdf)
   ├─ GUIA-JWT.md            Cómo funciona el login, con dibujos y analogías
   ├─ MAPA-ARCHIVOS.md       Qué hace cada archivo y dónde tocar cada cosa
   └─ PREGUNTAS-TEORIA.md    5 preguntas de Java, Oracle y Angular (+ JWT y Spring)
```

## Arranque rápido

```powershell
# 1. Oracle (la primera vez tarda 1-3 min). Esperar a "(healthy)"
docker compose up -d
docker compose ps

# 2. UN backend (el que pidan)
cd backend-quarkus ; .\mvnw.cmd quarkus:dev          # -> http://localhost:8081
#   o
cd backend-spring  ; .\mvnw.cmd spring-boot:run      # -> http://localhost:8080

# 3. Angular (otra terminal)
cd frontend ; npm install ; npx ng serve              # -> http://localhost:4200
```

**¿A qué backend se conecta Angular?** Una sola línea: `frontend/src/app/api.config.ts`

```ts
export const API_URL = 'http://localhost:8081';   // Quarkus   (Spring: 8080)
```

## Usuarios de prueba

| Usuario | Contraseña | Rol | Puede |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Listar, crear, editar, eliminar |
| `viewer` | `viewer123` | VIEW | Solo listar |
| `bloqueado` | `viewer123` | VIEW (inactivo) | Nada: el login lo rechaza |

## Contrato de la API (igual en los dos backends)

| Método | URL | Rol | Respuestas |
|---|---|---|---|
| POST | `/api/auth/login` | público | 200 `{token, username, rol, expiraEnSegundos}` · 401 · 400 |
| GET | `/api/auth/me` | ADMIN, VIEW | 200 `{username, roles}` |
| GET | `/api/contribuyentes` | ADMIN, VIEW | 200 |
| GET | `/api/contribuyentes/{id}` | ADMIN, VIEW | 200 · 404 |
| POST | `/api/contribuyentes` | ADMIN | 201 · 400 · 409 (NIT repetido) |
| PUT | `/api/contribuyentes/{id}` | ADMIN | 200 · 400 · 404 · 409 |
| DELETE | `/api/contribuyentes/{id}` | ADMIN | 204 · 404 |

Sin token → **401**. Token con rol VIEW en POST/PUT/DELETE → **403**.
Errores propios: `{ "status": 409, "mensaje": "..." }`.

## Qué está verificado

Se probaron los dos backends contra Oracle con las mismas 20 peticiones (login correcto/incorrecto/inactivo, 400, 401, 403, 404, 409, CRUD completo y CORS) y el frontend en un navegador con 16 escenarios (validaciones del login, roles, recarga de página, logout, CRUD y cierre de sesión automático con token inválido).

## Archivos que normalmente cambiarás en la prueba

| Qué | Dónde |
|---|---|
| Datos de conexión a Oracle | `backend-*/src/main/resources/application.properties` |
| Backend al que apunta Angular | `frontend/src/app/api.config.ts` |
| Reglas de qué rol puede qué | Quarkus: `@RolesAllowed` en `ContribuyenteResource.java` · Spring: `SecurityConfig.java` |
| Campos de la tabla | `db/01_schema.sql` + entidad + Request (DTO) + `contribuyente.service.ts` |
