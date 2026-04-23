package com.qroterritory.resource;

import org.openapi.quarkus.openapi_yaml.api.DelegacionesApi;
import org.openapi.quarkus.openapi_yaml.model.Delegacion;
import com.qroterritory.entity.DelegacionEntity;
import io.quarkus.cache.CacheResult;

import java.util.List;
import java.util.stream.Collectors;

public class DelegacionesResource implements DelegacionesApi {

    @Override
    @CacheResult(cacheName = "lista-delegaciones")
    public List<Delegacion> getDelegaciones() {

        System.out.println("Consultando MySQL - Cargando todas las delegaciones...");

        // 1. Usamos Panache para traer todos los registros de la tabla
        List<DelegacionEntity> entidades = DelegacionEntity.listAll();

        // 2. Mapeamos las entidades de base de datos a los DTOs de OpenAPI
        return entidades.stream().map(entidad -> {
            Delegacion dto = new Delegacion();
            dto.setId(entidad.id);
            dto.setNombre(entidad.nombre);
            return dto;
        }).collect(Collectors.toList());
    }
}