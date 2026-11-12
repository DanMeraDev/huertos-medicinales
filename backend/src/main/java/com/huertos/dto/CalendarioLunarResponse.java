package com.huertos.dto;

import com.huertos.model.FaseLunar;
import java.time.LocalDate;

public record CalendarioLunarResponse(
        Long id,
        FaseLunar faseLunar,
        String nombreKichwa,
        String nombreEspanol,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String practicasRecomendadas,
        String consejoKinti) {
}
