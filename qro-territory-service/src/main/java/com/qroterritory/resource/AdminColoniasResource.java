package com.qroterritory.resource;

import org.openapi.quarkus.openapi_yaml.api.AdminColoniasApi;
import org.openapi.quarkus.openapi_yaml.model.Colonia;
import org.openapi.quarkus.openapi_yaml.model.ColoniaInput;
import org.openapi.quarkus.openapi_yaml.model.TipoAsentamiento; // <-- ¡Nuevo Import!
import com.qroterritory.entity.ColoniaEntity;
import com.qroterritory.entity.DelegacionEntity;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.BadRequestException;
import io.quarkus.cache.CacheInvalidateAll;

import jakarta.annotation.security.RolesAllowed;
import io.quarkus.security.Authenticated;

public class AdminColoniasResource implements AdminColoniasApi {

    @Override
    @Transactional
    @CacheInvalidateAll(cacheName = "colonias-por-delegacion")
    public Colonia createColonia(ColoniaInput input) {

        ColoniaEntity nuevaColonia = new ColoniaEntity();
        nuevaColonia.nombre = input.getNombre();
        nuevaColonia.codigoPostal = input.getCodigoPostal();

        // CONVERSIÓN: De Enum (OpenAPI) a String (MySQL)
        if (input.getTipoAsentamiento() != null) {
            nuevaColonia.tipoAsentamiento = input.getTipoAsentamiento().name();
        }

        if (input.getDelegacionId() != null) {
            DelegacionEntity delegacion = DelegacionEntity.findById(input.getDelegacionId().longValue());
            if (delegacion == null) {
                throw new BadRequestException("La delegación especificada no existe.");
            }
            nuevaColonia.delegacion = delegacion;
        }

        nuevaColonia.persist();
        return mapearAColonia(nuevaColonia);
    }

    @Override
    @Transactional
    @CacheInvalidateAll(cacheName = "colonias-por-delegacion")
    public Colonia updateColonia(Long id, ColoniaInput input) {

        ColoniaEntity colonia = ColoniaEntity.findById(id);
        if (colonia == null) {
            throw new NotFoundException("La colonia con ID " + id + " no existe.");
        }

        colonia.nombre = input.getNombre();
        colonia.codigoPostal = input.getCodigoPostal();

        // CONVERSIÓN: De Enum (OpenAPI) a String (MySQL)
        if (input.getTipoAsentamiento() != null) {
            colonia.tipoAsentamiento = input.getTipoAsentamiento().name();
        } else {
            colonia.tipoAsentamiento = null;
        }

        if (input.getDelegacionId() != null) {
            DelegacionEntity delegacion = DelegacionEntity.findById(input.getDelegacionId().longValue());
            if (delegacion == null) {
                throw new BadRequestException("La delegación especificada no existe.");
            }
            colonia.delegacion = delegacion;
        } else {
            colonia.delegacion = null;
        }

        return mapearAColonia(colonia);
    }

    private Colonia mapearAColonia(ColoniaEntity entidad) {
        Colonia dto = new Colonia();
        dto.setId(entidad.id);
        dto.setNombre(entidad.nombre);
        dto.setCodigoPostal(entidad.codigoPostal);

        // CONVERSIÓN: De String (MySQL) a Enum (OpenAPI)
        if (entidad.tipoAsentamiento != null) {
            try {
                // valueOf busca el nombre exacto dentro de la lista de Enums permitidos
                dto.setTipoAsentamiento(TipoAsentamiento.valueOf(entidad.tipoAsentamiento));
            } catch (IllegalArgumentException e) {
                // Si por alguna razón MySQL tiene un texto que no está en el Enum, lo dejamos nulo para evitar crashear
                dto.setTipoAsentamiento(null);
            }
        }

        if (entidad.delegacion != null) {
            dto.setDelegacionId(entidad.delegacion.id);
        }
        return dto;
    }
}