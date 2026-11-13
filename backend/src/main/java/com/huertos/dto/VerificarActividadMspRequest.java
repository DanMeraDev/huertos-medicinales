package com.huertos.dto;

import com.huertos.model.EstadoVerificacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificarActividadMspRequest(
        @NotNull(message = "El dictamen de la actividad es obligatorio")
        EstadoVerificacion nuevoEstado,

        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion) {
}
