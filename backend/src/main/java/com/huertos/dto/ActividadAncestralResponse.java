package com.huertos.dto;

import com.huertos.model.EstadoVerificacion;
import java.time.LocalDate;
import java.util.List;

public record ActividadAncestralResponse(
        Long id,
        Long comunidadId,
        String tituloSaber,
        EstadoVerificacion estado,
        Integer numeroIntento,
        LocalDate fechaRealizacion,
        List<ParticipanteActividadDto> participantes
) {}
