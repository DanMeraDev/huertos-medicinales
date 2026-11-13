package com.huertos.dto;

public record CredencialesEmailDto(
        String destinatario,
        String nombreSolicitante,
        String nombreComunidad,
        String passwordTemporal) {
}
