package com.huertos.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DescuentoStockRequest(
        @NotNull(message = "El identificador del material en bodega es obligatorio")
        Long bodegaMaterialId,

        @NotNull(message = "La cantidad sembrada es obligatoria")
        @Positive(message = "La cantidad sembrada debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "La cantidad sembrada admite hasta 8 enteros y 2 decimales")
        BigDecimal cantidadSembrada
) {}
