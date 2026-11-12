package com.hacienda.auth;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

/**
 * Espejo de la tabla USUARIO.
 *
 * Panache "Active Record": la entidad trae sus propios métodos estáticos
 * (find, listAll, persist...) igual que un Model de Eloquent en Laravel.
 * Campos public = Panache genera getters/setters por debajo.
 */
@Entity
@Table(name = "USUARIO")
public class Usuario extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Oracle genera el id (IDENTITY)
    public Long id;

    public String username;

    @Column(name = "PASSWORD_HASH")
    public String passwordHash;   // BCrypt, nunca texto plano

    public String rol;            // "ADMIN" o "VIEW"

    public boolean activo;        // NUMBER(1): 1 = true, 0 = false

    /** SELECT * FROM usuario WHERE username = ? */
    public static Usuario porUsername(String username) {
        return find("username", username).firstResult();
    }
}
