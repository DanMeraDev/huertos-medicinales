package com.huertos.dto;

import java.time.LocalDate;

public record MiembroEstadoResponse(
        Long miembroId,
        Long comunidadId,
        String nombreMiembro,
        boolean activoEnComunidad,
        LocalDate fechaBaja,
        String motivoBaja,
        String mensaje
) {}
