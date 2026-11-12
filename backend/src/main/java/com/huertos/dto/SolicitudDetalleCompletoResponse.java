package com.huertos.dto;

import com.huertos.model.DisenoPrincipal;
import com.huertos.model.EstadoSolicitud;
import com.huertos.model.Genero;
import com.huertos.model.TipoOrganizacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudDetalleCompletoResponse(
        Long id,
        String nombreComunidad,
        TipoOrganizacion tipoOrganizacion,
        String provincia,
        String parroquia,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        int altitudMsnm,
        DisenoPrincipal disenoPrincipal,
        String subtiposSolicitados,
        String cedulaSolicitante,
        String nombreSolicitante,
        LocalDate fechaNacimientoSolicitante,
        Genero generoSolicitante,
        String correoSolicitante,
        String celularSolicitante,
        EstadoSolicitud estado,
        LocalDateTime creadoEn) {
}
