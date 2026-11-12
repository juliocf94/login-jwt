package com.hacienda.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * Convierte los errores de @Valid en un 400 con mensajes legibles:
 *   { "status": 400, "mensaje": "Datos inválidos", "errores": ["nit: El NIT es obligatorio", ...] }
 * (Sin esto Spring responde 400 pero sin decir qué campo falló.)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validacion(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(Map.of("status", 400, "mensaje", "Datos inválidos", "errores", errores));
    }
}
