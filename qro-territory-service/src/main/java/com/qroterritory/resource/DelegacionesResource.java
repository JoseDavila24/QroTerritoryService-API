package com.qroterritory.resource;

import com.qroterritory.entity.DelegacionEntity;
import com.qroterritory.filter.CacheStatus;
import io.quarkus.cache.CacheResult;
import jakarta.inject.Inject;
import org.openapi.quarkus.openapi_yaml.api.DelegacionesApi;
import org.openapi.quarkus.openapi_yaml.model.Delegacion;

import java.util.List;
import java.util.stream.Collectors;

public class DelegacionesResource implements DelegacionesApi {

    @Inject
    CacheStatus cacheStatus;

    @Override
    @CacheResult(cacheName = "lista-delegaciones")
    public List<Delegacion> getDelegaciones() {
        cacheStatus.markMiss();

        List<DelegacionEntity> entidades = DelegacionEntity.listAll();

        return entidades.stream().map(entidad -> {
            Delegacion dto = new Delegacion();
            dto.setId(entidad.id);
            dto.setNombre(entidad.nombre);
            dto.setSede(entidad.sede);
            return dto;
        }).collect(Collectors.toList());
    }
}
