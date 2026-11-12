package com.huertos.dto;

import com.huertos.model.TipoMaterialVegetal;
import com.huertos.model.UnidadMedida;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarMaterialVegetalRequest(
        @NotNull(message = "La planta es obligatoria")
        Long plantaId,

        @NotNull(message = "El tipo de material es obligatorio")
        TipoMaterialVegetal tipoMaterial,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "La cantidad admite hasta 8 enteros y 2 decimales")
        BigDecimal cantidad,

        @NotNull(message = "La unidad de medida es obligatoria")
        UnidadMedida unidadMedida,

        String loteOrigen,

        @NotNull(message = "La fecha de ingreso es obligatoria")
        LocalDate fechaIngreso
) {}
