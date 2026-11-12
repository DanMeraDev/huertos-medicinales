package com.huertos.dto;

import com.huertos.model.TipoMaterialVegetal;
import com.huertos.model.UnidadMedida;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MaterialVegetalResponse(
        Long id,
        Long comunidadId,
        Long plantaId,
        String nombreComunPlanta,
        String nombreCientificoPlanta,
        TipoMaterialVegetal tipoMaterial,
        BigDecimal cantidadDisponible,
        UnidadMedida unidadMedida,
        String loteOrigen,
        LocalDate fechaIngreso
) {}
