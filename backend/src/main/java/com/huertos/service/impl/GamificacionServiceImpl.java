package com.huertos.service.impl;

import com.huertos.config.ConstantesGamificacion;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.model.Miembro;
import com.huertos.model.NivelMiembro;
import com.huertos.repository.MiembroRepository;
import com.huertos.service.GamificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamificacionServiceImpl implements GamificacionService {

    private final MiembroRepository miembroRepository;

    @Override
    @Transactional
    public NivelMiembro evaluarAscensoNivel(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro no encontrado con id: " + miembroId));

        NivelMiembro nivelAnterior = miembro.getNivel();
        evaluarAscensoNivelEntidad(miembro);

        if (miembro.getNivel() != nivelAnterior) {
            miembroRepository.save(miembro);
            log.info("Miembro id {} ascendió honoríficamente a nivel {}", miembro.getId(), miembro.getNivel());
        }

        return miembro.getNivel();
    }

    private void evaluarAscensoNivelEntidad(Miembro miembro) {
        int puntos = miembro.getPuntosTotales();
        NivelMiembro actual = miembro.getNivel();

        if (puntos >= ConstantesGamificacion.UMBRAL_MAESTRO) {
            if (actual != NivelMiembro.MAESTRO) {
                miembro.setNivel(NivelMiembro.MAESTRO);
            }
        } else if (puntos >= ConstantesGamificacion.UMBRAL_OFICIAL) {
            if (actual == null || actual == NivelMiembro.APRENDIZ) {
                miembro.setNivel(NivelMiembro.OFICIAL);
            }
        } else if (actual == null) {
            miembro.setNivel(NivelMiembro.APRENDIZ);
        }
    }
}
