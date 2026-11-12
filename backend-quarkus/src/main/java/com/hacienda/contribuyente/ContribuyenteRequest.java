package com.hacienda.contribuyente;

import jakarta.validation.constraints.*;

/**
 * Body del POST / PUT. Las anotaciones validan ANTES de entrar al método
 * (si fallan, Quarkus responde 400 solo).
 */
public record ContribuyenteRequest(
        @NotBlank(message = "El NIT es obligatorio") @Size(max = 20) String nit,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) String nombre,
        @Email(message = "Email inválido") @Size(max = 150) String email,
        @NotBlank @Pattern(regexp = "PERSONA|EMPRESA", message = "Tipo debe ser PERSONA o EMPRESA") String tipo
) {}
