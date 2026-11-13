package com.huertos.mapper;

import com.huertos.dto.ActividadAncestralResponse;
import com.huertos.dto.ActividadParaCorregirResponse;
import com.huertos.dto.ActividadRevisionResponse;
import com.huertos.dto.ParticipanteActividadDto;
import com.huertos.dto.ParticipanteRevisionResponse;
import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.ActividadMspParticipante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActividadAncestralMapper {

    @Mapping(target = "comunidadId", source = "comunidad.id")
    @Mapping(target = "tituloSaber", source = "saberAncestral.titulo")
    @Mapping(target = "estado", source = "estadoVerificacion")
    ActividadAncestralResponse toActividadAncestralResponse(ActividadAncestralMsp entidad);

    @Mapping(target = "tituloSaber", source = "saberAncestral.titulo")
    @Mapping(target = "estado", source = "estadoVerificacion")
    ActividadParaCorregirResponse toActividadParaCorregirResponse(ActividadAncestralMsp entidad);

    @Mapping(target = "comunidadId", source = "comunidad.id")
    @Mapping(target = "nombreComunidad", source = "comunidad.nombre")
    @Mapping(target = "tituloSaber", source = "saberAncestral.titulo")
    @Mapping(target = "estado", source = "estadoVerificacion")
    ActividadRevisionResponse toActividadRevisionResponse(ActividadAncestralMsp entidad);

    @Mapping(target = "miembroId", source = "miembro.id")
    ParticipanteActividadDto toParticipanteDto(ActividadMspParticipante participante);

    @Mapping(target = "miembroId", source = "miembro.id")
    @Mapping(target = "nombreCompleto", expression = "java(participante.getMiembro().getNombres() + \" \" + participante.getMiembro().getApellidos())")
    ParticipanteRevisionResponse toParticipanteRevisionResponse(ActividadMspParticipante participante);
}
