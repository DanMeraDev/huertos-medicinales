package com.huertos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoMiembroRequest(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo,

        @Size(max = 255, message = "El motivo de baja no puede superar 255 caracteres")
        String motivoBaja
) {}
