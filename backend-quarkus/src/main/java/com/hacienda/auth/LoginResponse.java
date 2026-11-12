package com.hacienda.auth;

/** Respuesta del login. Angular guarda el token y usa "rol" para mostrar u ocultar botones. */
public record LoginResponse(String token, String username, String rol, long expiraEnSegundos) {}
