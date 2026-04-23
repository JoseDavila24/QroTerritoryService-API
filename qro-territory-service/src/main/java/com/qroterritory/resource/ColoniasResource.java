package com.qroterritory.resource;

import org.openapi.quarkus.openapi_yaml.api.ColoniasApi;
import org.openapi.quarkus.openapi_yaml.model.Colonia;
import org.openapi.quarkus.openapi_yaml.model.PageResponseColonias;
import com.qroterritory.entity.ColoniaEntity;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

public class ColoniasResource implements ColoniasApi {

    @Override
    public Colonia getColoniaById(Long id) {
        ColoniaEntity entidad = ColoniaEntity.findById(id);

        if (entidad == null) {
            throw new NotFoundException("La colonia con ID " + id + " no existe.");
        }

        Colonia dto = new Colonia();
        dto.setId(entidad.id);
        dto.setNombre(entidad.nombre);
        dto.setCodigoPostal(entidad.codigoPostal);

        return dto;
    }

    @Override
    public PageResponseColonias getColoniasByDelegacion(Long delegacionId, Integer page, Integer size) {

        List<ColoniaEntity> entidades = ColoniaEntity.find("delegacion.id", delegacionId).list();

        List<Colonia> listaColonias = entidades.stream().map(entidad -> {
            Colonia dto = new Colonia();
            dto.setId(entidad.id);
            dto.setNombre(entidad.nombre);
            dto.setCodigoPostal(entidad.codigoPostal);

            // ¡Agregamos el ID de la delegación al JSON!
            if (entidad.delegacion != null) {
                dto.setDelegacionId(entidad.delegacion.id);
            }

            return dto;
        }).collect(Collectors.toList());

        PageResponseColonias respuesta = new PageResponseColonias();

        // ¡La solución al error de compilación!
        respuesta.setContent(listaColonias);

        // Llenamos los demás campos requeridos por tu YAML
        respuesta.setPage(page != null ? page : 0);
        respuesta.setSize(size != null ? size : 100);

        // total_elements SÍ pide un Long, así que dejamos el casteo
        respuesta.setTotalElements((long) listaColonias.size());

        // ¡Pero total_pages pide un Integer! Así que le quitamos la 'L'
        respuesta.setTotalPages(1);

        return respuesta;
    }
}