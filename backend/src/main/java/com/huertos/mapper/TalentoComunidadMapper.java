package com.huertos.mapper;

import com.huertos.dto.CrearTalentoComunidadRequest;
import com.huertos.dto.TalentoComunidadResponse;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.Comunidad;
import com.huertos.model.TalentoComunidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TalentoComunidadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comunidad", source = "comunidad")
    @Mapping(target = "actividadComunidad", source = "actividad")
    @Mapping(target = "nombreInsignia", source = "request.nombreInsignia")
    @Mapping(target = "descripcion", source = "request.descripcion")
    @Mapping(target = "metaRepeticiones", source = "request.metaRepeticiones")
    @Mapping(target = "iconoCodigo", constant = "insignia-semilla")
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "creadoEn", ignore = true)
    TalentoComunidad toEntity(CrearTalentoComunidadRequest request, Comunidad comunidad, ActividadComunidad actividad);

    @Mapping(target = "id", source = "talento.id")
    @Mapping(target = "comunidadId", source = "talento.comunidad.id")
    @Mapping(target = "actividadComunidadId", source = "talento.actividadComunidad.id")
    @Mapping(target = "nombreActividad", source = "talento.actividadComunidad.nombre")
    @Mapping(target = "nombreInsignia", source = "talento.nombreInsignia")
    @Mapping(target = "descripcion", source = "talento.descripcion")
    @Mapping(target = "metaRepeticiones", source = "talento.metaRepeticiones")
    @Mapping(target = "iconoCodigo", source = "talento.iconoCodigo")
    @Mapping(target = "activo", source = "talento.activo")
    @Mapping(target = "creadoEn", source = "talento.creadoEn")
    TalentoComunidadResponse toResponse(TalentoComunidad talento);
}
