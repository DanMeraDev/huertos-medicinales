package com.huertos.dto;

import com.huertos.model.CausaFracaso;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RegistrarBajaMaterialRequest(
        @NotNull(message = "La cantidad a dar de baja es obligatoria")
        @Positive(message = "La cantidad a dar de baja debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "La cantidad a dar de baja admite hasta 8 enteros y 2 decimales")
        BigDecimal cantidadBaja,

        @NotNull(message = "Debe seleccionar una causa de fracaso válida para el registro de la baja")
        CausaFracaso causaFracaso,

        @Size(max = 300, message = "La observación no puede superar los 300 caracteres")
        String observacion
) {}

