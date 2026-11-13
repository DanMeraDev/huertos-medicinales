package com.huertos.service;

import com.huertos.model.NivelMiembro;

public interface GamificacionService {

    void procesarPuntosActividadMsp(Long actividadId);

    NivelMiembro evaluarAscensoNivel(Long miembroId);

    void otorgarInsigniaOficial(Long actividadId);

    /**
     * Calcula y acredita puntos de una actividad cotidiana comunitaria exclusivamente
     * a la cuenta del miembro voluntario, evaluando su ascenso de nivel y desbloqueando
     * el talento comunitario en caso de alcanzar la meta de repeticiones acumuladas.
     *
     * @param registroActividadId identificador del registro de actividad cotidiana
     */
    void procesarActividadCotidiana(Long registroActividadId);
}
