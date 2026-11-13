package com.huertos.dto;

import com.huertos.model.EstadoVerificacion;
import java.time.LocalDateTime;

public record ResultadoVerificacionResponse(
        Long actividadId,
        EstadoVerificacion estado,
        Integer puntosComunidadAcreditados,
        LocalDateTime fechaVerificacion) {
}
