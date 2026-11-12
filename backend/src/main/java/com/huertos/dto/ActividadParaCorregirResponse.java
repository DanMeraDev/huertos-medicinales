package com.huertos.dto;

import com.huertos.model.EstadoVerificacion;

public record ActividadParaCorregirResponse(
        Long id,
        String tituloSaber,
        EstadoVerificacion estado,
        Integer numeroIntento,
        String observacionMsp,
        String descripcionBitacora,
        String evidenciaFotoUrl,
        String evidenciaVideoUrl
) {}
