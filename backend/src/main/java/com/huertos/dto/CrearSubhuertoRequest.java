package com.huertos.dto;

import com.huertos.model.SubtipoSubhuerto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CrearSubhuertoRequest(
        @NotBlank(message = "El nombre de la zona es obligatorio")
        @Size(max = 100, message = "El nombre de la zona no puede superar los 100 caracteres")
        String nombreZona,
        @NotNull(message = "El subtipo de subhuerto es obligatorio")
        SubtipoSubhuerto subtipo,
        @NotNull(message = "El área en m² es obligatoria")
        @Positive(message = "El área debe ser mayor a cero")
        @Digits(integer = 6, fraction = 2, message = "El área debe tener máximo 6 enteros y 2 decimales")
        BigDecimal areaM2
) {}