package com.qroterritory.resource;

import org.openapi.quarkus.openapi_yaml.api.ColoniasApi;
import org.openapi.quarkus.openapi_yaml.model.Colonia;
import org.openapi.quarkus.openapi_yaml.model.PageResponseColonias;
import org.openapi.quarkus.openapi_yaml.model.TipoAsentamiento; // <-- ¡Nuevo Import!
import com.qroterritory.entity.ColoniaEntity;
import jakarta.ws.rs.NotFoundException;
import io.quarkus.cache.CacheResult;

import java.util.List;
import java.util.stream.Collectors;

public class ColoniasResource implements ColoniasApi {

    @Override
    public Colonia getColoniaById(Long id) {
        ColoniaEntity entidad = ColoniaEntity.findById(id);

        if (entidad == null) {
            throw new NotFoundException("La colonia con ID " + id + " no existe.");
        }

        return mapearAColonia(entidad);
    }

    @Override
    @CacheResult(cacheName = "colonias-por-delegacion")
    public PageResponseColonias getColoniasByDelegacion(Long delegacionId, Integer page, Integer size) {

        int numPage = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 100;

        var query = ColoniaEntity.find("delegacion.id", delegacionId).page(numPage, pageSize);
        List<ColoniaEntity> entidades = query.list();

        // Transformamos usando nuestro nuevo método auxiliar
        List<Colonia> listaColonias = entidades.stream()
                .map(this::mapearAColonia)
                .collect(Collectors.toList());

        PageResponseColonias respuesta = new PageResponseColonias();
        respuesta.setContent(listaColonias);
        respuesta.setPage(numPage);
        respuesta.setSize(pageSize);
        respuesta.setTotalElements(query.count());
        respuesta.setTotalPages(query.pageCount());

        return respuesta;
    }

    // =================================================================
    // Método auxiliar para centralizar la conversión
    // =================================================================
    private Colonia mapearAColonia(ColoniaEntity entidad) {
        Colonia dto = new Colonia();
        dto.setId(entidad.id);
        dto.setNombre(entidad.nombre);
        dto.setCodigoPostal(entidad.codigoPostal);

        // CONVERSIÓN: De String (MySQL) a Enum (OpenAPI)
        if (entidad.tipoAsentamiento != null) {
            try {
                dto.setTipoAsentamiento(TipoAsentamiento.valueOf(entidad.tipoAsentamiento));
            } catch (IllegalArgumentException e) {
                dto.setTipoAsentamiento(null);
            }
        }

        if (entidad.delegacion != null) {
            dto.setDelegacionId(entidad.delegacion.id);
        }

        return dto;
    }
}