package com.huertos.service.impl;

import com.huertos.model.NivelMiembro;
import com.huertos.repository.MiembroRepository;
import com.huertos.service.GamificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GamificacionServiceImpl implements GamificacionService {

    private final MiembroRepository miembroRepository;

    @Override
    @Transactional
    public NivelMiembro evaluarAscensoNivel(Long miembroId) {
        throw new UnsupportedOperationException("Pendiente de implementacion");
    }
}
