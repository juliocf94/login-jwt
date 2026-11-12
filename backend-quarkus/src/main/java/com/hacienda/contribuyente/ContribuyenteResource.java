package com.hacienda.contribuyente;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * CRUD de contribuyentes protegido por rol.
 *
 *   VIEW  -> solo GET (listar / ver uno)
 *   ADMIN -> todo
 *
 * Sin token          -> 401 Unauthorized (automático)
 * Token con rol VIEW -> 403 Forbidden en POST/PUT/DELETE (automático)
 */
@Path("/api/contribuyentes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "VIEW"})          // regla por defecto para toda la clase
public class ContribuyenteResource {

    @GET
    public List<Contribuyente> listar() {
        return Contribuyente.listAll(io.quarkus.panache.common.Sort.by("id"));
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") Long id) {
        Contribuyente c = Contribuyente.findById(id);
        return c == null ? noExiste(id) : Response.ok(c).build();
    }

    @POST
    @Transactional
    @RolesAllowed("ADMIN")                // sobrescribe la regla de la clase
    public Response crear(@Valid ContribuyenteRequest req) {
        if (Contribuyente.existeNit(req.nit().trim(), null)) {
            return error(409, "Ya existe un contribuyente con NIT " + req.nit());
        }
        Contribuyente c = new Contribuyente();
        copiar(req, c);
        c.persist();                      // INSERT
        return Response.created(URI.create("/api/contribuyentes/" + c.id)).entity(c).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response actualizar(@PathParam("id") Long id, @Valid ContribuyenteRequest req) {
        Contribuyente c = Contribuyente.findById(id);
        if (c == null) return noExiste(id);
        if (Contribuyente.existeNit(req.nit().trim(), id)) {
            return error(409, "Ya existe otro contribuyente con NIT " + req.nit());
        }
        copiar(req, c);                   // el UPDATE sale solo al terminar la transacción
        return Response.ok(c).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response eliminar(@PathParam("id") Long id) {
        return Contribuyente.deleteById(id) ? Response.noContent().build() : noExiste(id);
    }

    // ---------- ayudantes ----------
    private static void copiar(ContribuyenteRequest req, Contribuyente c) {
        c.nit = req.nit().trim();
        c.nombre = req.nombre().trim();
        c.email = req.email();
        c.tipo = req.tipo();
    }

    private static Response noExiste(Long id) {
        return error(404, "Contribuyente " + id + " no existe");
    }

    /** Mismo formato de error que el backend Spring: { "status": 409, "mensaje": "..." } */
    private static Response error(int status, String mensaje) {
        return Response.status(status).entity(Map.of("status", status, "mensaje", mensaje)).build();
    }
}
