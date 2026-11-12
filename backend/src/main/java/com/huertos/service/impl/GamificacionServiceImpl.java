package com.huertos.service.impl;

import com.huertos.model.NivelMiembro;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.service.GamificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GamificacionServiceImpl implements GamificacionService {

    private final ComunidadRepository comunidadRepository;
    private final HuertoRepository huertoRepository;
    private final MiembroRepository miembroRepository;
    private final ActividadAncestralMspRepository actividadMspRepository;
    private final MiembroInsigniaRepository miembroInsigniaRepository;

    @Override
    @Transactional
    public void procesarPuntosActividadMsp(Long actividadId) {
        throw new UnsupportedOperationException("Pendiente de implementacion");
    }

    @Override
    public NivelMiembro evaluarAscensoNivel(Long miembroId) {
        throw new UnsupportedOperationException("Pendiente de implementacion");
    }

    @Override
    @Transactional
    public void otorgarInsigniaOficial(Long actividadId) {
        throw new UnsupportedOperationException("Pendiente de implementacion");
    }
}
