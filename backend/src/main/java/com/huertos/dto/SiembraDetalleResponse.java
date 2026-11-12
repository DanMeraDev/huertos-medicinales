package com.huertos.dto;

import com.huertos.model.CualidadTermica;
import com.huertos.model.EstadoCultivo;
import com.huertos.model.FaseLunar;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SiembraDetalleResponse(
        Long id,
        Long subhuertoId,
        String nombreSubhuerto,
        Long bodegaMaterialId,
        Long plantaId,
        String nombreComunPlanta,
        CualidadTermica cualidadTermica,
        BigDecimal cantidadSembrada,
        LocalDate fechaSiembra,
        FaseLunar faseLunarSiembra,
        boolean tuvoAlertaAltitud,
        EstadoCultivo estadoCultivo
) {}

