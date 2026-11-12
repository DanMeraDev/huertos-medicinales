package com.huertos.mapper;

import com.huertos.dto.ActualizarMiembroRequest;
import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.MiembroEstadoResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import com.huertos.model.ComunidadMiembro;
import com.huertos.model.Miembro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MiembroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cedula", ignore = true)
    @Mapping(target = "nombres", ignore = true)
    @Mapping(target = "apellidos", ignore = true)
    @Mapping(target = "fechaNacimiento", ignore = true)
    @Mapping(target = "nivel", ignore = true)
    @Mapping(target = "puntosTotales", ignore = true)
    @Mapping(target = "consentimientoDatos", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "consentimientoEn", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    void actualizarDesdeRequest(ActualizarMiembroRequest request, @MappingTarget Miembro miembro);

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

    @Mapping(target = "miembroId", source = "relacion.miembro.id")
    @Mapping(target = "comunidadId", source = "relacion.comunidad.id")
    @Mapping(target = "nombreMiembro", expression = "java(relacion.getMiembro().getNombres() + \" \" + relacion.getMiembro().getApellidos())")
    @Mapping(target = "activoEnComunidad", source = "relacion.activoEnComunidad")
    @Mapping(target = "fechaBaja", source = "relacion.fechaBaja")
    @Mapping(target = "motivoBaja", source = "relacion.motivoBaja")
    @Mapping(target = "mensaje", source = "mensaje")
    MiembroEstadoResponse toEstadoResponse(ComunidadMiembro relacion, String mensaje);
}
