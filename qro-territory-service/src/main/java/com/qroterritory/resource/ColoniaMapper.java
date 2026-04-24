package com.qroterritory.resource;

import com.qroterritory.entity.ColoniaEntity;
import org.openapi.quarkus.openapi_yaml.model.Colonia;
import org.openapi.quarkus.openapi_yaml.model.TipoAsentamiento;

final class ColoniaMapper {

    private ColoniaMapper() {}

    static Colonia toDto(ColoniaEntity entidad) {
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
