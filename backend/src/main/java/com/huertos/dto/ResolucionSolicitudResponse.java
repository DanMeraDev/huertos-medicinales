package com.huertos.dto;

import com.huertos.model.EstadoSolicitud;

public record ResolucionSolicitudResponse(
        Long solicitudId,
        EstadoSolicitud estado,
        String mensaje,
        Long comunidadId,
        Long usuarioId) {
}
