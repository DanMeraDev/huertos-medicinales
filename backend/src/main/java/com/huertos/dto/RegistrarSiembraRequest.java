package com.huertos.dto;

import com.huertos.model.FaseLunar;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarSiembraRequest(
        @NotNull(message = "El subhuerto es obligatorio")
        Long subhuertoId,

        @NotNull(message = "El material vegetal es obligatorio")
        Long bodegaMaterialId,

        @NotNull(message = "La cantidad sembrada es obligatoria")
        @Positive(message = "La cantidad sembrada debe ser positiva")
        @Digits(integer = 8, fraction = 2, message = "La cantidad sembrada debe tener máximo 8 enteros y 2 decimales")
        BigDecimal cantidadSembrada,

        @NotNull(message = "La fecha de siembra es obligatoria")
        @PastOrPresent(message = "La fecha de siembra no puede ser futura")
        LocalDate fechaSiembra,

        @NotNull(message = "La fase lunar es obligatoria")
        FaseLunar faseLunarSiembra,

        boolean aceptaRiesgoAltitud
) {}

