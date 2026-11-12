package com.hacienda.auth;

import jakarta.persistence.*;

/** Espejo de la tabla USUARIO. */
@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;   // BCrypt, nunca texto plano

    private String rol;            // "ADMIN" o "VIEW"

    private boolean activo;        // NUMBER(1): 1 = true, 0 = false

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }
}
