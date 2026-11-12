package com.huertos.dto;

import com.huertos.model.EstadoSolicitud;
import com.huertos.model.TipoOrganizacion;
import java.time.LocalDateTime;

public record SolicitudComunidadResponse(
        Long id,
        String nombreComunidad,
        TipoOrganizacion tipoOrganizacion,
        String provincia,
        String parroquia,
        String nombreSolicitante,
        String correoSolicitante,
        EstadoSolicitud estado,
        LocalDateTime creadoEn) {
}
