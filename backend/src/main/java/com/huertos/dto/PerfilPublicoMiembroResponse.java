package com.huertos.dto;

import com.huertos.model.NivelMiembro;
import java.util.List;

public record PerfilPublicoMiembroResponse(
        String nombreCompleto,
        NivelMiembro nivel,
        Integer puntosTotales,
        List<String> comunidades,
        List<InsigniaObtenidaDto> insignias
) {}
