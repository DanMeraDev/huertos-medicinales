package com.huertos.dto;

import java.time.LocalDateTime;

public record TalentoComunidadResponse(
        Long id,
        Long comunidadId,
        Long actividadComunidadId,
        String nombreActividad,
        String nombreInsignia,
        String descripcion,
        Integer metaRepeticiones,
        String iconoCodigo,
        boolean activo,
        LocalDateTime creadoEn
) {}
