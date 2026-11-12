package com.hacienda.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body del POST /api/auth/login  ->  { "username": "admin", "password": "admin123" } */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio") @Size(max = 50) String username,
        @NotBlank(message = "La contraseña es obligatoria") @Size(max = 100) String password
) {}
