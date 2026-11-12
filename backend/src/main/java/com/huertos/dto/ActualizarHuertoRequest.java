package com.huertos.dto;

import com.huertos.model.DisenoPrincipal;
import com.huertos.model.FuenteAgua;
import com.huertos.model.TipoAbono;
import com.huertos.model.TipoCerca;
import com.huertos.model.TipoSuelo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ActualizarHuertoRequest(
        @NotNull BigDecimal latitud,
        @NotNull BigDecimal longitud,
        @NotNull @PositiveOrZero Integer altitudMsnm,
        String orientacion,
        @NotNull @DecimalMin("16.00") @DecimalMax("400.00") BigDecimal areaTotalM2,
        @NotNull DisenoPrincipal disenoPrincipal,
        TipoSuelo tipoSuelo,
        FuenteAgua fuenteAgua,
        TipoAbono tipoAbono,
        TipoCerca tipoCerca,
        @Size(max = 255) String detalleCerca) {
}

