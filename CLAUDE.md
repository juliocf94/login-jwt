# Contexto para el asistente (Claude / Gemini)

## Situación
Estoy haciendo una **prueba técnica con tiempo limitado**. Prioridad: **que funcione**. Respuestas directas y en español, cambios mínimos y probados, sin refactorizar lo que ya funciona. Explicaciones cortas, y si sirve, con una analogía con PHP, Laravel, Node o React (es lo que domino).

Reglas de la prueba:
- Login con **JWT**.
- 2 roles: **ADMIN** (CRUD completo) y **VIEW** (solo listar).
- **Oracle en Docker**.
- Backend en **Quarkus o Spring Boot** (el que pidan).
- Frontend en **Angular**.

No priorizar CSS ni diseño: funcionalidad y validaciones.

## Estructura
```
docker-compose.yml   Oracle 23 Free (gvenzl/oracle-free:23-slim), contenedor oracle-login, usuario app/app123, FREEPDB1
db/01_schema.sql     Tablas USUARIO (login) y CONTRIBUYENTE (CRUD) + datos. Se ejecuta solo la 1a vez del contenedor
backend-quarkus/     Quarkus 3.40, Java 21, puerto 8081. Panache Active Record. JWT RS256 (privateKey.pem / publicKey.pem)
backend-spring/      Spring Boot 4.1.2, Java 21, puerto 8080. Spring Data JPA. JWT HS256 (app.jwt.secret)
frontend/            Angular 22 standalone, ZONELESS (estado en signals), SIN SSR, puerto 4200
docs/                Guías (.md) y PDFs. build_pdf.py regenera los PDFs
postman/             Colección con login que guarda el token
```

## Comandos
```powershell
docker compose up -d ; docker compose ps          # Oracle, esperar (healthy)
cd backend-quarkus ; .\mvnw quarkus:dev            # :8081  Swagger /q/swagger-ui
cd backend-spring  ; .\mvnw spring-boot:run        # :8080  Swagger /swagger-ui.html
cd frontend ; npm install ; npx ng serve           # :4200
```
No hay Maven global: siempre usar `mvnw`. No hay Angular CLI global: usar `npx ng`.

## Contrato de la API (IDÉNTICO en los dos backends; no romperlo)
| Método | URL | Rol | Respuestas |
|---|---|---|---|
| POST | /api/auth/login `{username,password}` | público | 200 `{token,username,rol,expiraEnSegundos}` · 401 · 400 |
| GET | /api/auth/me | ADMIN, VIEW | 200 `{username,roles}` |
| GET | /api/contribuyentes, /{id} | ADMIN, VIEW | 200 · 404 |
| POST | /api/contribuyentes | ADMIN | 201 + Location · 400 · 409 (NIT repetido) |
| PUT | /api/contribuyentes/{id} | ADMIN | 200 · 400 · 404 · 409 |
| DELETE | /api/contribuyentes/{id} | ADMIN | 204 · 404 |

- Sin token: 401. Rol insuficiente: 403.
- Errores propios: `{status, mensaje}`. El 400 de Spring añade `errores[]`; el de Quarkus trae `violations[]`. `frontend/src/app/core/mensaje-error.ts` entiende los dos.
- El rol viaja en el claim `groups` (lista) y también en `rol`. El usuario va en `upn`/`sub`.
- Usuarios: `admin/admin123` (ADMIN), `viewer/viewer123` (VIEW), `bloqueado` (inactivo: el login lo rechaza con el mismo 401).

## Dónde se toca cada cosa
| Qué | Dónde |
|---|---|
| Conexión a Oracle | `backend-*/src/main/resources/application.properties` |
| Backend al que apunta Angular | `frontend/src/app/api.config.ts` (una línea, `API_URL`). **No agregar un selector de backend en la UI** |
| Permisos por rol | Quarkus: `@RolesAllowed` en `ContribuyenteResource`. Spring: `config/SecurityConfig.java` |
| Duración del token | `app.jwt.duracion-segundos` (3600) en los dos properties |
| Login | Quarkus `auth/AuthResource.java` (BcryptUtil + Jwt.sign). Spring `auth/AuthController.java` (PasswordEncoder + JwtEncoder) |
| Angular: sesión y token | `core/auth.service.ts` (signals, localStorage), `core/auth.interceptor.ts` (Bearer; 401 => logout), `core/auth.guard.ts` |
| Campo nuevo en contribuyente | SQL (ALTER TABLE) → entidad → `ContribuyenteRequest` (los dos backends) → `contribuyente.service.ts` → `contribuyentes.html` |

## Convenciones
- Código simple, con comentarios en español; nada de capas extra si no las piden.
- Quarkus: entidad Panache con campos `public` y métodos estáticos; sin Repository ni Service.
- Spring: Controller → JpaRepository; las reglas de rol solo en SecurityConfig.
- `ddl-auto=none` / `schema-management.strategy=none`: Hibernate **no** toca las tablas; los cambios van en SQL.
- Angular: componentes standalone con `inject()`, estado en `signal()`/`computed()` (si se asigna una variable normal en un `subscribe`, la vista NO se refresca), control de flujo `@if`/`@for`, formularios reactivos con Validators.
- Mantener el mismo contrato en los dos backends si se cambia uno.

## Problemas ya conocidos
- `quarkus.devservices.enabled=false` es obligatorio; sin eso Quarkus intenta levantar su propio Oracle en Docker.
- Puerto 1521 ocupado: hay otro Oracle (`docker ps`, `docker stop <nombre>`).
- Cambios en `db/*.sql` no se aplican si el volumen ya existe: `docker compose down -v` y volver a subir.
- Un 401 en el login es "clave incorrecta": el interceptor lo excluye a propósito para no hacer logout.
- `quarkus-elytron-security-common` (BcryptUtil) y `springdoc` no aparecen en los generadores web: van a mano en el pom.
- "No se pudo conectar con el servidor" en Angular = backend apagado, `API_URL` incorrecta o CORS (`quarkus.http.cors.origins` / `app.cors.origen`).

## Cómo verificar un cambio
1. Swagger: login → copiar el token → Authorize → probar el endpoint (con `viewer`, POST debe dar 403).
2. Angular: `admin` ve el formulario; `viewer` solo la tabla; F5 mantiene la sesión.
