package com.huertos.mapper;

import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.MiembroResumenResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import com.huertos.model.ComunidadMiembro;
import com.huertos.model.Miembro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MiembroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "puntosTotales", constant = "0")
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "consentimientoEn", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    Miembro toMiembro(RegistrarMiembroRequest request);

    @Mapping(target = "id", source = "miembro.id")
    @Mapping(target = "comunidadId", source = "relacion.comunidad.id")
    @Mapping(target = "nombreCompleto", expression = "java(miembro.getNombres() + \" \" + miembro.getApellidos())")
    @Mapping(target = "nombres", source = "miembro.nombres")
    @Mapping(target = "apellidos", source = "miembro.apellidos")
    @Mapping(target = "fechaNacimiento", source = "miembro.fechaNacimiento")
    @Mapping(target = "edad", source = "edad")
    @Mapping(target = "genero", source = "miembro.genero")
    @Mapping(target = "telefono", source = "miembro.telefono")
    @Mapping(target = "nivel", source = "miembro.nivel")
    @Mapping(target = "puntosTotales", source = "miembro.puntosTotales")
    @Mapping(target = "activoEnComunidad", source = "relacion.activoEnComunidad")
    @Mapping(target = "fechaIngreso", source = "relacion.fechaIngreso")
    MiembroDetalleResponse toDetalleResponse(Miembro miembro, ComunidadMiembro relacion, int edad);

    @Mapping(target = "id", source = "miembro.id")
    @Mapping(target = "nombreCompleto", expression = "java(miembro.getNombres() + \" \" + miembro.getApellidos())")
    MiembroResumenResponse toResumenResponse(Miembro miembro);
}
