package com.qroterritory.resource;

import org.openapi.quarkus.openapi_yaml.api.DelegacionesApi;
import org.openapi.quarkus.openapi_yaml.model.Delegacion;
import com.qroterritory.entity.DelegacionEntity;
import java.util.List;
import java.util.stream.Collectors;

public class DelegacionesResource implements DelegacionesApi {

    @Override
    public List<Delegacion> getDelegaciones() {
        // 1. Panache hace la consulta SELECT * FROM delegaciones por nosotros
        List<DelegacionEntity> entidades = DelegacionEntity.listAll();

        // 2. Convertimos la lista de Base de Datos a la lista que espera OpenAPI
        return entidades.stream().map(entidad -> {
            Delegacion dto = new Delegacion();
            dto.setId(entidad.id);
            dto.setNombre(entidad.nombre);
            dto.setSede(entidad.sede);
            return dto;
        }).collect(Collectors.toList());
    }
}