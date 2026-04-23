package com.qroterritory.resource;

import com.qroterritory.entity.ColoniaEntity;
import com.qroterritory.filter.CacheStatus;
import io.quarkus.cache.CacheResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.openapi.quarkus.openapi_yaml.api.ColoniasApi;
import org.openapi.quarkus.openapi_yaml.model.Colonia;
import org.openapi.quarkus.openapi_yaml.model.PageResponseColonias;
import org.openapi.quarkus.openapi_yaml.model.TipoAsentamiento;

import java.util.List;
import java.util.stream.Collectors;

public class ColoniasResource implements ColoniasApi {

    @Inject
    CacheStatus cacheStatus;

    @Override
    public Colonia getColoniaById(Long id) {
        cacheStatus.markMiss();

        ColoniaEntity entidad = ColoniaEntity.findById(id);
        if (entidad == null) {
            throw new NotFoundException("Colonia con id " + id + " no encontrada.");
        }
        return mapearAColonia(entidad);
    }

    @Override
    @CacheResult(cacheName = "colonias-por-delegacion")
    public PageResponseColonias getColoniasByDelegacion(Long delegacionId, Integer page, Integer size) {
        cacheStatus.markMiss();

        int numPage = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0 && size <= 250) ? size : 100;

        var query = ColoniaEntity.find("delegacion.id", delegacionId).page(numPage, pageSize);
        List<ColoniaEntity> entidades = query.list();
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

    private Colonia mapearAColonia(ColoniaEntity entidad) {
        Colonia dto = new Colonia();
        dto.setId(entidad.id);
        dto.setNombre(entidad.nombre);
        dto.setCodigoPostal(entidad.codigoPostal);

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
