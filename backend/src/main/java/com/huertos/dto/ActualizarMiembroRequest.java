package com.huertos.dto;

import com.huertos.model.Genero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ActualizarMiembroRequest(
        @NotNull(message = "El género es obligatorio")
        Genero genero,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^09[0-9]{8}$", message = "El teléfono debe iniciar con 09 y tener 10 dígitos")
        String telefono
) {}
