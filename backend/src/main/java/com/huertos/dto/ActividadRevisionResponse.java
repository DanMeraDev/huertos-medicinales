package com.huertos.dto;

import com.huertos.model.EstadoVerificacion;
import com.huertos.model.FaseLunar;
import java.time.LocalDate;
import java.util.List;

public record ActividadRevisionResponse(
        Long id,
        Long comunidadId,
        String nombreComunidad,
        String tituloSaber,
        LocalDate fechaRealizacion,
        FaseLunar faseLunarEjecucion,
        String descripcionBitacora,
        String evidenciaFotoUrl,
        String evidenciaVideoUrl,
        Integer numeroIntento,
        EstadoVerificacion estado,
        List<ParticipanteRevisionResponse> participantes) {
}
