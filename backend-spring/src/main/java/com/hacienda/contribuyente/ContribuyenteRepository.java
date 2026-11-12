package com.hacienda.contribuyente;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository ya trae findAll, findById, save, deleteById, existsById...
 * Solo agregamos las consultas por NIT (el SQL sale del nombre del método).
 */
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {
    boolean existsByNit(String nit);
    boolean existsByNitAndIdNot(String nit, Long id);   // para el PUT: otro con mi NIT
}
