package com.huertos.dto;

import java.time.LocalDate;

public record InsigniaObtenidaDto(
        String nombre,
        String descripcion,
        String tipo,
        LocalDate fechaObtencion
) {}
