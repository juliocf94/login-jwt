package com.hacienda.contribuyente;

import jakarta.persistence.*;

/** Espejo de la tabla CONTRIBUYENTE. */
@Entity
@Table(name = "CONTRIBUYENTE")
public class Contribuyente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nit;
    private String nombre;
    private String email;
    private String tipo;   // "PERSONA" o "EMPRESA" (CHECK en Oracle)

    public Long getId() { return id; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
