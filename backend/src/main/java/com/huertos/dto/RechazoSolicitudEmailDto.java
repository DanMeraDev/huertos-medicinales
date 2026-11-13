package com.huertos.dto;

public record RechazoSolicitudEmailDto(
        String destinatario,
        String nombreSolicitante,
        String nombreComunidad,
        String motivoRechazo) {
}
