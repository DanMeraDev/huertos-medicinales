package com.huertos.service;

import com.huertos.dto.CalendarioLunarResponse;
import java.time.LocalDate;

public interface CalendarioLunarService {

    /**
     * Devuelve la fase lunar que rige en la fecha consultada.
     *
     * @throws com.huertos.exception.RecursoNoEncontradoException si no hay fase configurada para esa fecha
     */
    CalendarioLunarResponse obtenerFaseActual(LocalDate fecha);
}
