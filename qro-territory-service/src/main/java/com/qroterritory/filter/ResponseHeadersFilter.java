package com.qroterritory.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.UUID;

@Provider
public class ResponseHeadersFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME = "req.startTime";

    @Inject
    CacheStatus cacheStatus;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        req.setProperty(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        // X-Request-Id: propagate client header or generate new UUID
        String requestId = req.getHeaderString("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        res.getHeaders().putSingle("X-Request-Id", requestId);

        // X-Response-Time in milliseconds
        Long start = (Long) req.getProperty(START_TIME);
        if (start != null) {
            res.getHeaders().putSingle("X-Response-Time", (System.currentTimeMillis() - start) + "ms");
        }

        // Cache-Control and X-Cache only for successful GET responses
        if ("GET".equalsIgnoreCase(req.getMethod()) && res.getStatus() == 200) {
            res.getHeaders().putSingle("Cache-Control", "public, max-age=600");
            res.getHeaders().putSingle("X-Cache", cacheStatus.isMiss() ? "MISS" : "HIT");
        }
    }
}
