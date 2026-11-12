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
        NivelMiembro nuevoNivel = determinarNuevoNivel(miembro.getPuntosTotales(), nivelAnterior);

        if (nuevoNivel != nivelAnterior) {
            miembro.setNivel(nuevoNivel);
            miembroRepository.save(miembro);
            log.info("Miembro id {} ascendió honoríficamente a nivel {}", miembro.getId(), nuevoNivel);
        }

        return nuevoNivel;
    }

    private NivelMiembro determinarNuevoNivel(int puntosTotales, NivelMiembro nivelActual) {
        if (puntosTotales >= ConstantesGamificacion.UMBRAL_MAESTRO) {
            return NivelMiembro.MAESTRO;
        }
        if (puntosTotales >= ConstantesGamificacion.UMBRAL_OFICIAL) {
            return (nivelActual == NivelMiembro.MAESTRO) ? NivelMiembro.MAESTRO : NivelMiembro.OFICIAL;
        }
        return (nivelActual == null) ? NivelMiembro.APRENDIZ : nivelActual;
    }
}
