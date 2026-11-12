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

## Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| Java (JDK) | **21** | Los dos backends (`java -version` para comprobar) |
| Docker Desktop | cualquiera reciente | Oracle |
| Node.js + npm | compatible con Angular 22 | Frontend |

No hace falta Maven instalado: cada backend trae su `mvnw` (Maven Wrapper). En Mac/Linux usa `./mvnw` en lugar de `.\mvnw.cmd`.

## Configuración de Oracle

### Datos de conexión

| Dato | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `1521` |
| Servicio (PDB) | `FREEPDB1` |
| Usuario de la app | `app` |
| Contraseña | `app123` |
| Usuario administrador | `system` / `admin123` (también `sys` como SYSDBA) |
| URL JDBC | `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` |

Para DBeaver o SQL Developer: conexión tipo **Service Name** = `FREEPDB1` (no SID), usuario `app` / `app123`.

### Opción A: Oracle con el `docker-compose.yml` de este proyecto

```powershell
docker compose up -d        # crea el contenedor oracle-login (imagen gvenzl/oracle-free:23-slim)
docker compose ps           # esperar a que diga (healthy): la 1a vez tarda 1-3 min
docker compose logs -f      # opcional: ver el avance; listo cuando dice "DATABASE IS READY TO USE"
```

La primera vez, el contenedor crea el usuario `app` y ejecuta `db/01_schema.sql` (tablas `USUARIO` y `CONTRIBUYENTE` con datos de prueba).

| Comando | Qué hace |
|---|---|
| `docker compose stop` / `start` | Apaga / enciende Oracle sin perder datos |
| `docker compose down` | Borra el contenedor, **conserva** los datos (volumen `oracle-data`) |
| `docker compose down -v` | Borra contenedor **y** datos; al volver a subir se ejecuta otra vez `db/*.sql` |

### Opción B: ya tengo un Oracle (el de la prueba u otro)

1. Crear las tablas: conectarse como el usuario que te den y ejecutar `db/01_schema.sql` **desde `CREATE TABLE` hacia abajo** (sin la línea `CONNECT`).
2. Cambiar los datos de conexión en el backend que uses:

**Quarkus**: `backend-quarkus/src/main/resources/application.properties`
```properties
quarkus.datasource.db-kind=oracle
quarkus.datasource.jdbc.url=jdbc:oracle:thin:@//HOST:PUERTO/SERVICIO
quarkus.datasource.username=USUARIO
quarkus.datasource.password=CLAVE
quarkus.devservices.enabled=false     # obligatorio: si no, Quarkus intenta levantar su propio Oracle
```

**Spring Boot**: `backend-spring/src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:oracle:thin:@//HOST:PUERTO/SERVICIO
spring.datasource.username=USUARIO
spring.datasource.password=CLAVE
```

Formato de la URL: `@//host:puerto/servicio` es con **nombre de servicio** (lo normal). Si te dan un **SID**, la URL es `jdbc:oracle:thin:@host:puerto:SID`.

Hibernate **no crea ni modifica tablas** (`ddl-auto=none` / `schema-management.strategy=none`): todo cambio de tablas va en SQL.

## Correr el backend Quarkus

```powershell
cd backend-quarkus
.\mvnw.cmd quarkus:dev
```

- API: http://localhost:8081
- Swagger: http://localhost:8081/q/swagger-ui
- En modo `dev` recarga solo al guardar cambios. Para salir: `q` o `Ctrl+C`.
- JWT firmado con **RS256**: `src/main/resources/privateKey.pem` (firma) y `publicKey.pem` (verificación).
- CORS permitido para `http://localhost:4200` (`quarkus.http.cors.origins`).

Generar el ejecutable (opcional):
```powershell
.\mvnw.cmd clean package
java -jar target/quarkus-app/quarkus-run.jar
```

## Correr el backend Spring Boot

```powershell
cd backend-spring
.\mvnw.cmd spring-boot:run
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Para salir: `Ctrl+C`.
- JWT firmado con **HS256** usando la clave `app.jwt.secret` del `application.properties`.
- CORS permitido para `http://localhost:4200` (`app.cors.origen`).

Generar el ejecutable (opcional):
```powershell
.\mvnw.cmd clean package
java -jar target/backend-spring-0.0.1-SNAPSHOT.jar
```

### Probar que el backend funciona

1. Abrir Swagger y ejecutar `POST /api/auth/login` con `{"username":"admin","password":"admin123"}`.
2. Copiar el `token` de la respuesta → botón **Authorize** → pegarlo.
3. Ejecutar `GET /api/contribuyentes`: debe devolver la lista. Con `viewer`, un `POST` debe dar **403**.

## Problemas comunes

| Síntoma | Causa / solución |
|---|---|
| El backend no conecta a Oracle (`ORA-12541`, `Connection refused`) | Oracle no está arriba o aún no está `(healthy)`: `docker compose ps` |
| `ORA-12514` (servicio desconocido) | El servicio de la URL no es correcto: debe ser `FREEPDB1`, no `FREE` ni `XE` |
| `ORA-01017` (usuario/clave inválidos) | Revisar usuario y clave en `application.properties` |
| `ORA-00942` (tabla no existe) | No se ejecutó `db/01_schema.sql` o te conectaste con otro usuario |
| Puerto 1521 ocupado | Hay otro Oracle corriendo: `docker ps` y `docker stop <nombre>`, o mapear `"1522:1521"` y cambiar el puerto en la URL |
| Cambié `db/*.sql` y no se aplica | El volumen ya existe: `docker compose down -v` y `docker compose up -d` |
| Puerto 8080 / 8081 ocupado | Cerrar el otro proceso o cambiar `server.port` (Spring) / `quarkus.http.port` (Quarkus) |
| Angular dice "No se pudo conectar con el servidor" | Backend apagado, `API_URL` equivocada o CORS |
| Errores raros después de cambiar código | Limpiar compilados: `.\mvnw.cmd clean` |

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
