package com.huertos.dto;

import com.huertos.model.NivelMiembro;

public record RankingMiembroResponse(
        Integer posicion,
        Long miembroId,
        String nombres,
        String apellidos,
        NivelMiembro nivel,
        Integer puntosTotales,
        Long totalInsignias
) {
}

