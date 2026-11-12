package com.huertos.mapper;

import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.SolicitudComunidadResponse;
import com.huertos.model.SolicitudComunidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudComunidadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaNacimientoSolicitante", source = "fechaNacimiento")
    @Mapping(target = "estado", constant = "PENDIENTE")
    @Mapping(target = "consentimientoEn", ignore = true)
    @Mapping(target = "motivoRechazo", ignore = true)
    @Mapping(target = "revisadoPor", ignore = true)
    @Mapping(target = "fechaRevision", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    SolicitudComunidad toSolicitudComunidad(CrearSolicitudComunidadRequest request);

    SolicitudComunidadResponse toSolicitudComunidadResponse(SolicitudComunidad solicitud);
}
