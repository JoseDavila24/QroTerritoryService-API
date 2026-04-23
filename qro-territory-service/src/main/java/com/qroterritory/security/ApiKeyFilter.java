package com.qroterritory.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

@Provider
public class ApiKeyFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "admin.api.key", defaultValue = "Qro-Secret-Key-2026")
    String apiKey;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Usamos la URL absoluta completa para que no haya dudas (ej. http://localhost:8080/api/v1/admin/colonias/5)
        String urlCompleta = requestContext.getUriInfo().getAbsolutePath().toString();

        // Imprimimos en la terminal lo que el filtro está viendo
        System.out.println("🕵️ Filtro de seguridad revisando la URL: " + urlCompleta);

        // Si la URL contiene "admin" en cualquier parte, activamos las defensas
        if (urlCompleta.contains("admin")) {

            String headerKey = requestContext.getHeaderString("X-API-KEY");
            System.out.println("🔑 Llave recibida en el Header: " + headerKey);

            if (headerKey == null || !headerKey.equals(apiKey)) {
                System.out.println("🚫 ACCESO DENEGADO");
                requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                .entity("{\"error\": \"Acceso denegado. X-API-KEY inválida o ausente.\"}")
                                .header("Content-Type", "application/json")
                                .build()
                );
            } else {
                System.out.println("✅ ACCESO PERMITIDO");
            }
        }
    }
}