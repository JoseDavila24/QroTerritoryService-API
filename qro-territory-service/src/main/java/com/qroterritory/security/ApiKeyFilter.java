package com.qroterritory.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openapi.quarkus.openapi_yaml.model.ErrorResponse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Provider
public class ApiKeyFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "admin.api.key", defaultValue = "Qro-Secret-Key-2026")
    String apiKey;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String urlCompleta = requestContext.getUriInfo().getAbsolutePath().toString();

        if (!urlCompleta.contains("admin")) {
            return;
        }

        String headerKey = requestContext.getHeaderString("X-API-KEY");

        if (headerKey == null || !headerKey.equals(apiKey)) {
            String path = requestContext.getUriInfo().getAbsolutePath().getPath();

            ErrorResponse er = new ErrorResponse();
            er.setStatus(401);
            er.setError("Unauthorized");
            er.setMessage("API Key inválida o no proporcionada en el header X-API-KEY.");
            er.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            er.setPath(path);

            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(er)
                    .type(MediaType.APPLICATION_JSON)
                    .build()
            );
        }
    }
}
