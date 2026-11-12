package com.hacienda.contribuyente;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

/** Espejo de la tabla CONTRIBUYENTE (Active Record, como un Model de Laravel). */
@Entity
@Table(name = "CONTRIBUYENTE")
public class Contribuyente extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nit;
    public String nombre;
    public String email;
    public String tipo;   // "PERSONA" o "EMPRESA" (CHECK en Oracle)

    /** ¿Existe otro contribuyente con ese NIT? (idExcluir = el que estoy editando) */
    public static boolean existeNit(String nit, Long idExcluir) {
        return idExcluir == null
                ? count("nit", nit) > 0
                : count("nit = ?1 and id <> ?2", nit, idExcluir) > 0;
    }
}
