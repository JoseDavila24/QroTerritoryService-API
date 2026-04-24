package com.qroterritory.resource;

import com.qroterritory.entity.ColoniaEntity;
import com.qroterritory.entity.DelegacionEntity;
import io.quarkus.cache.CacheInvalidateAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.openapi.quarkus.openapi_yaml.model.ColoniaInput;
import org.openapi.quarkus.openapi_yaml.model.ErrorResponse;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Path("/admin/colonias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminColoniasResource {

    private static final Logger LOG = Logger.getLogger(AdminColoniasResource.class);

    @POST
    @Transactional
    @CacheInvalidateAll(cacheName = "colonias-por-delegacion")
    public Response createColonia(@Valid ColoniaInput input, @Context UriInfo uriInfo) {
        String path = uriInfo.getAbsolutePath().getPath();

        // Validate codigo_postal format → 400
        if (input.getCodigoPostal() == null || !input.getCodigoPostal().matches("^\\d{5}$")) {
            return errorResponse(400, "Bad Request",
                "El campo 'codigo_postal' debe contener exactamente 5 dígitos numéricos.", path);
        }

        // Validate delegation exists → 422
        DelegacionEntity delegacion = DelegacionEntity.findById(input.getDelegacionId().longValue());
        if (delegacion == null) {
            return errorResponse(422, "Unprocessable Entity",
                "La delegación con id " + input.getDelegacionId() + " no existe en el catálogo.", path);
        }

        // Duplicate name in same delegation → 409
        long duplicados = ColoniaEntity.count("nombre = ?1 and delegacion.id = ?2",
            input.getNombre(), input.getDelegacionId());
        if (duplicados > 0) {
            return errorResponse(409, "Conflict",
                "Ya existe una colonia con el nombre '" + input.getNombre() + "' en la delegación " + input.getDelegacionId() + ".", path);
        }

        ColoniaEntity nueva = new ColoniaEntity();
        nueva.nombre = input.getNombre();
        nueva.codigoPostal = input.getCodigoPostal();
        nueva.tipoAsentamiento = input.getTipoAsentamiento() != null ? input.getTipoAsentamiento().name() : null;
        nueva.delegacion = delegacion;
        nueva.persist();

        URI location = uriInfo.getBaseUriBuilder()
            .path("colonias/{id}")
            .resolveTemplate("id", nueva.id)
            .build();

        return Response.created(location).entity(ColoniaMapper.toDto(nueva)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @CacheInvalidateAll(cacheName = "colonias-por-delegacion")
    public Response updateColonia(@PathParam("id") Long id, @Valid ColoniaInput input, @Context UriInfo uriInfo) {
        String path = uriInfo.getAbsolutePath().getPath();

        // Validate codigo_postal format → 400
        if (input.getCodigoPostal() == null || !input.getCodigoPostal().matches("^\\d{5}$")) {
            return errorResponse(400, "Bad Request",
                "El campo 'codigo_postal' debe contener exactamente 5 dígitos numéricos.", path);
        }

        ColoniaEntity colonia = ColoniaEntity.findById(id);
        if (colonia == null) {
            return errorResponse(404, "Not Found", "Colonia con id " + id + " no encontrada.", path);
        }

        // Capture old delegation id before update (lazy load while still in transaction)
        Long anteriorId = colonia.delegacion != null ? colonia.delegacion.id : null;

        // Validate target delegation exists → 422
        DelegacionEntity delegacion = DelegacionEntity.findById(input.getDelegacionId().longValue());
        if (delegacion == null) {
            return errorResponse(422, "Unprocessable Entity",
                "La delegación con id " + input.getDelegacionId() + " no existe en el catálogo.", path);
        }

        // Duplicate name in target delegation excluding self → 409
        long duplicados = ColoniaEntity.count("nombre = ?1 and delegacion.id = ?2 and id != ?3",
            input.getNombre(), input.getDelegacionId(), id);
        if (duplicados > 0) {
            return errorResponse(409, "Conflict",
                "Ya existe una colonia con el nombre '" + input.getNombre() + "' en la delegación " + input.getDelegacionId() + ".", path);
        }

        if (!delegacion.id.equals(anteriorId)) {
            LOG.infof("Colonia %d movida de Delegación %d a %d.", id, anteriorId, delegacion.id);
        }

        colonia.nombre = input.getNombre();
        colonia.codigoPostal = input.getCodigoPostal();
        colonia.tipoAsentamiento = input.getTipoAsentamiento() != null ? input.getTipoAsentamiento().name() : null;
        colonia.delegacion = delegacion;

        return Response.ok(ColoniaMapper.toDto(colonia)).build();
    }

    private Response errorResponse(int status, String error, String message, String path) {
        ErrorResponse er = new ErrorResponse();
        er.setStatus(status);
        er.setError(error);
        er.setMessage(message);
        er.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        er.setPath(path);
        return Response.status(status).entity(er).type(MediaType.APPLICATION_JSON).build();
    }
}
