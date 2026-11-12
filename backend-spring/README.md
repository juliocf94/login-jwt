# backend-spring · puerto 8080

```powershell
.\mvnw.cmd spring-boot:run    # Ctrl+C para salir (sin recarga automática: reiniciar tras cambios)
```

- Swagger: http://localhost:8080/swagger-ui.html (login → copiar token → botón **Authorize**)
- Configuración: `src/main/resources/application.properties`
- Seguridad completa: `src/main/java/com/hacienda/config/SecurityConfig.java`

Ver `../docs/MAPA-ARCHIVOS.md` y `../docs/GUIA-JWT.md`.
