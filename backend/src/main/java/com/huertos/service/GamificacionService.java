package com.huertos.service;

import com.huertos.model.NivelMiembro;

public interface GamificacionService {

    void procesarPuntosActividadMsp(Long actividadId);

    NivelMiembro evaluarAscensoNivel(Long miembroId);

    void otorgarInsigniaOficial(Long actividadId);

    void procesarActividadCotidiana(Long registroActividadId);
}
