package com.huertos.dto;

import com.huertos.model.CategoriaSaber;
import com.huertos.model.FaseLunar;

public record SaberAncestralResumenResponse(
        Long id,
        Integer ordenPaso,
        String codigoHito,
        String titulo,
        String tituloKichwa,
        CategoriaSaber categoria,
        FaseLunar faseLunarIdeal,
        String nombreInsigniaOficial) {
}
