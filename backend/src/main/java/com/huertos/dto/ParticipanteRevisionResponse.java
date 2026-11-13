package com.huertos.dto;

public record ParticipanteRevisionResponse(
        Long miembroId,
        String nombreCompleto,
        Integer horasTrabajadas) {
}
