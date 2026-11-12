package com.huertos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RechazarSolicitudRequest(
        @NotBlank(message = "El motivo del rechazo es obligatorio")
        @Size(min = 20, max = 500, message = "El motivo del rechazo debe tener entre 20 y 500 caracteres")
        String motivoRechazo) {
}
