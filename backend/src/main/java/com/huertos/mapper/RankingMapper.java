package com.huertos.mapper;

import com.huertos.dto.RankingComunidadResponse;
import com.huertos.model.Comunidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RankingMapper {

    @Mapping(target = "posicion", source = "posicion")
    @Mapping(target = "comunidadId", source = "comunidad.id")
    @Mapping(target = "nombreComunidad", source = "comunidad.nombre")
    @Mapping(target = "tipoOrganizacion", source = "comunidad.tipoOrganizacion")
    @Mapping(target = "provincia", source = "comunidad.provincia")
    @Mapping(target = "parroquia", source = "comunidad.parroquia")
    @Mapping(target = "puntosComunidad", source = "comunidad.puntosComunidad")
    RankingComunidadResponse toResponse(Comunidad comunidad, Integer posicion);
}
