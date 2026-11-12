package com.hacienda.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * LOGIN con JWT.
 *
 * Flujo:
 *   1. Angular manda usuario + contraseña           POST /api/auth/login
 *   2. Buscamos el usuario en Oracle y comparamos la contraseña con BCrypt
 *   3. Si es correcto, firmamos un JWT con su rol (claim "groups")
 *   4. Angular guarda el token y lo manda en cada petición:
 *        Authorization: Bearer <token>
 *   5. smallrye-jwt valida el token solo; @RolesAllowed revisa el rol.
 */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "app.jwt.duracion-segundos")
    long duracionSegundos;

    @Inject
    JsonWebToken jwt;   // el token de la petición actual (ya validado)

    @POST
    @Path("/login")
    @PermitAll   // público: aquí todavía no hay token
    public Response login(@Valid LoginRequest req) {
        Usuario u = Usuario.porUsername(req.username().trim());

        // Mismo mensaje para "no existe", "clave mala" o "inactivo":
        // así no le decimos a un atacante qué usuarios existen.
        if (u == null || !u.activo || !BcryptUtil.matches(req.password(), u.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("status", 401, "mensaje", "Usuario o contraseña incorrectos"))
                    .build();
        }

        String token = Jwt.issuer(issuer)
                .upn(u.username)                  // quién es
                .groups(Set.of(u.rol))            // su rol -> lo lee @RolesAllowed
                .claim("rol", u.rol)              // copia legible para Angular
                .expiresIn(Duration.ofSeconds(duracionSegundos))
                .sign();                          // firma con privateKey.pem

        return Response.ok(new LoginResponse(token, u.username, u.rol, duracionSegundos)).build();
    }

    /** Devuelve quién soy según el token. Útil para probar que el token funciona. */
    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "VIEW"})
    public Map<String, Object> me() {
        return Map.of("username", jwt.getName(), "roles", jwt.getGroups());
    }
}
