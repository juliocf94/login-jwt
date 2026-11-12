package com.hacienda.auth;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * LOGIN con JWT.
 *
 * Flujo:
 *   1. Angular manda usuario + contraseña           POST /api/auth/login
 *   2. Buscamos el usuario en Oracle y comparamos la contraseña con BCrypt
 *   3. Si es correcto, firmamos un JWT con su rol (claim "groups")
 *   4. Angular guarda el token y lo manda en cada petición:
 *        Authorization: Bearer <token>
 *   5. SecurityConfig valida el token y revisa el rol.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.issuer}")            String issuer;
    @Value("${app.jwt.duracion-segundos}") long duracionSegundos;

    // Inyección por constructor (Spring la hace sola; equivale a @Inject en Quarkus)
    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Usuario u = usuarios.findByUsername(req.username().trim()).orElse(null);

        // Mismo mensaje para "no existe", "clave mala" o "inactivo":
        // así no le decimos a un atacante qué usuarios existen.
        if (u == null || !u.isActivo() || !passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "mensaje", "Usuario o contraseña incorrectos"));
        }

        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(u.getUsername())                 // quién es
                .claim("upn", u.getUsername())
                .claim("groups", List.of(u.getRol()))     // su rol -> lo lee SecurityConfig
                .claim("rol", u.getRol())                 // copia legible para Angular
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(duracionSegundos))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return ResponseEntity.ok(new LoginResponse(token, u.getUsername(), u.getRol(), duracionSegundos));
    }

    /** Devuelve quién soy según el token. Útil para probar que el token funciona. */
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("username", jwt.getSubject(), "roles", jwt.getClaimAsStringList("groups"));
    }
}
