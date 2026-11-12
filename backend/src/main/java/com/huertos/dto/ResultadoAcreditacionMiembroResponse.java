package com.huertos.dto;

import com.huertos.model.NivelMiembro;
import java.util.List;

public record ResultadoAcreditacionMiembroResponse(
        Long registroId,
        Long miembroId,
        Integer puntosOtorgados,
        Integer puntosTotales,
        NivelMiembro nivel,
        boolean subioDeNivel,
        List<String> talentosDesbloqueados
) {}
