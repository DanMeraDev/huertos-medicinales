package com.huertos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DarBajaPlantaRequest(
        @NotBlank(message = "El motivo de la baja es obligatorio")
        @Size(max = 300, message = "El motivo de la baja no puede superar los 300 caracteres")
        String motivo) {
}
