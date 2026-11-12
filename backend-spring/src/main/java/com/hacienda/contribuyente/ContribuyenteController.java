package com.hacienda.contribuyente;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * CRUD de contribuyentes. Los permisos por rol NO están aquí,
 * están en SecurityConfig (GET = ADMIN/VIEW, el resto = ADMIN).
 *
 * Para la prueba se deja simple (controller -> repository).
 * En un proyecto grande, la lógica (NIT duplicado, etc.) iría en un @Service.
 */
@RestController
@RequestMapping("/api/contribuyentes")
public class ContribuyenteController {

    private final ContribuyenteRepository repo;

    public ContribuyenteController(ContribuyenteRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Contribuyente> listar() {
        return repo.findAll(Sort.by("id"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return repo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> noExiste(id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> crear(@Valid @RequestBody ContribuyenteRequest req) {
        if (repo.existsByNit(req.nit().trim())) {
            return error(409, "Ya existe un contribuyente con NIT " + req.nit());
        }
        Contribuyente c = new Contribuyente();
        copiar(req, c);
        c = repo.save(c);                                    // INSERT
        return ResponseEntity.created(URI.create("/api/contribuyentes/" + c.getId())).body(c);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ContribuyenteRequest req) {
        Contribuyente c = repo.findById(id).orElse(null);
        if (c == null) return noExiste(id);
        if (repo.existsByNitAndIdNot(req.nit().trim(), id)) {
            return error(409, "Ya existe otro contribuyente con NIT " + req.nit());
        }
        copiar(req, c);
        return ResponseEntity.ok(repo.save(c));              // UPDATE
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) return noExiste(id);
        repo.deleteById(id);                                 // DELETE
        return ResponseEntity.noContent().build();
    }

    // ---------- ayudantes ----------
    private static void copiar(ContribuyenteRequest req, Contribuyente c) {
        c.setNit(req.nit().trim());
        c.setNombre(req.nombre().trim());
        c.setEmail(req.email());
        c.setTipo(req.tipo());
    }

    private static ResponseEntity<?> noExiste(Long id) {
        return error(404, "Contribuyente " + id + " no existe");
    }

    /** Mismo formato de error que el backend Quarkus: { "status": 409, "mensaje": "..." } */
    private static ResponseEntity<?> error(int status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("status", status, "mensaje", mensaje));
    }
}
