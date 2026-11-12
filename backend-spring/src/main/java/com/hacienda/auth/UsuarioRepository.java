package com.hacienda.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data genera el SQL a partir del NOMBRE del método:
 *   findByUsername -> SELECT * FROM usuario WHERE username = ?
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
