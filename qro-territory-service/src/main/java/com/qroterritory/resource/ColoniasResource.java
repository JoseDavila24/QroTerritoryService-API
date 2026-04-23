package com.qroterritory.resource;

import org.openapi.quarkus.openapi_yaml.api.ColoniasApi;
import org.openapi.quarkus.openapi_yaml.model.Colonia;
import org.openapi.quarkus.openapi_yaml.model.PageResponseColonias;
import com.qroterritory.entity.ColoniaEntity;
import jakarta.ws.rs.NotFoundException;
import io.quarkus.cache.CacheResult;

import java.util.List;
import java.util.stream.Collectors;

public class ColoniasResource implements ColoniasApi {

    // =================================================================
    // 1. Obtener una colonia individual por su ID
    // =================================================================
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

        if (entidad.delegacion != null) {
            dto.setDelegacionId(entidad.delegacion.id);
        }

        return dto;
    }

    // =================================================================
    // 2. Obtener colonias de una delegación (¡CON CACHÉ Y PAGINACIÓN!)
    // =================================================================
    @Override
    @CacheResult(cacheName = "colonias-por-delegacion")
    public PageResponseColonias getColoniasByDelegacion(Long delegacionId, Integer page, Integer size) {

        // 1. Valores por defecto seguros para la paginación
        int numPage = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 100;

        System.out.println("Consultando MySQL - Delegación: " + delegacionId + " | Página: " + numPage);

        // 2. Magia de Panache: Creamos la consulta y le aplicamos la paginación
        var query = ColoniaEntity.find("delegacion.id", delegacionId).page(numPage, pageSize);

        // 3. Ejecutamos la consulta para obtener solo los registros de esta página
        List<ColoniaEntity> entidades = query.list();

        // 4. Transformamos las entidades a DTOs
        List<Colonia> listaColonias = entidades.stream().map(entidad -> {
            Colonia dto = new Colonia();
            dto.setId(entidad.id);
            dto.setNombre(entidad.nombre);
            dto.setCodigoPostal(entidad.codigoPostal);

            if (entidad.delegacion != null) {
                dto.setDelegacionId(entidad.delegacion.id);
            }
            return dto;
        }).collect(Collectors.toList());

        // 5. Armamos la respuesta con los datos reales de la consulta
        PageResponseColonias respuesta = new PageResponseColonias();
        respuesta.setContent(listaColonias);
        respuesta.setPage(numPage);
        respuesta.setSize(pageSize);

        // Panache cuenta automáticamente el total de registros y páginas por nosotros
        respuesta.setTotalElements(query.count());
        respuesta.setTotalPages(query.pageCount());

        return respuesta;
    }
}