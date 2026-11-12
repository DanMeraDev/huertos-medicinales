package com.huertos.service.impl;

import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.ActividadMspParticipante;
import com.huertos.model.Comunidad;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.Huerto;
import com.huertos.model.Miembro;
import com.huertos.model.MiembroInsignia;
import com.huertos.model.NivelMiembro;
import com.huertos.model.SaberAncestral;
import com.huertos.model.TipoOrigenInsignia;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.service.GamificacionService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamificacionServiceImpl implements GamificacionService {

    private static final int UMBRAL_OFICIAL = 50;
    private static final int UMBRAL_MAESTRO = 150;

    private final ComunidadRepository comunidadRepository;
    private final HuertoRepository huertoRepository;
    private final MiembroRepository miembroRepository;
    private final ActividadAncestralMspRepository actividadMspRepository;
    private final MiembroInsigniaRepository miembroInsigniaRepository;

    @Override
    @Transactional
    public void procesarPuntosActividadMsp(Long actividadId) {
        ActividadAncestralMsp actividad = actividadMspRepository.findById(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad ancestral no encontrada con id: " + actividadId));

        if (actividad.getEstadoVerificacion() != EstadoVerificacion.APROBADA) {
            throw new ReglaNegocioException("No se pueden otorgar puntos de gamificación en actividades no aprobadas");
        }

        int puntosBase = actividad.getSaberAncestral() != null ? actividad.getSaberAncestral().getPuntosBaseComunidad() : 20;
        int puntosComunidad;
        if (actividad.getNumeroIntento() == 1) {
            puntosComunidad = puntosBase;
        } else {
            puntosComunidad = Math.round(puntosBase * 0.70f);
        }
        actividad.setPuntosAcreditarComunidad(puntosComunidad);

        Comunidad comunidad = actividad.getComunidad();
        if (comunidad != null) {
            comunidad.setPuntosComunidad(comunidad.getPuntosComunidad() + puntosComunidad);
            comunidadRepository.save(comunidad);
        }

        Huerto huerto = actividad.getHuerto();
        if (huerto != null) {
            huerto.setPuntosHuerto(huerto.getPuntosHuerto() + puntosComunidad);
            huertoRepository.save(huerto);
        }

        if (actividad.getParticipantes() != null) {
            for (ActividadMspParticipante participante : actividad.getParticipantes()) {
                int puntosVoluntario = participante.getHorasTrabajadas() * 1;
                participante.setPuntosAcreditados(puntosVoluntario);

                Miembro miembro = participante.getMiembro();
                if (miembro != null) {
                    Miembro miembroPersistido = miembro.getId() != null
                            ? miembroRepository.findById(miembro.getId()).orElse(miembro)
                            : miembro;

                    miembroPersistido.setPuntosTotales(miembroPersistido.getPuntosTotales() + puntosVoluntario);
                    evaluarAscensoNivelEntidad(miembroPersistido);
                    miembroRepository.save(miembroPersistido);

                    if (!miembroPersistido.isActivo()) {
                        log.info("Miembro inactivo id {} acumuló {} puntos sin notificaciones activas.",
                                miembroPersistido.getId(), puntosVoluntario);
                    }
                }
            }
        }

        otorgarInsigniaOficial(actividadId);

        actividadMspRepository.save(actividad);
    }

    @Override
    public NivelMiembro evaluarAscensoNivel(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro no encontrado con id: " + miembroId));

        evaluarAscensoNivelEntidad(miembro);
        return miembro.getNivel();
    }

    private void evaluarAscensoNivelEntidad(Miembro miembro) {
        int puntos = miembro.getPuntosTotales();
        NivelMiembro actual = miembro.getNivel();

        if (puntos >= UMBRAL_MAESTRO) {
            if (actual != NivelMiembro.MAESTRO) {
                miembro.setNivel(NivelMiembro.MAESTRO);
                log.info("Miembro id {} ascendió a nivel MAESTRO", miembro.getId());
            }
        } else if (puntos >= UMBRAL_OFICIAL) {
            if (actual == null || actual == NivelMiembro.APRENDIZ) {
                miembro.setNivel(NivelMiembro.OFICIAL);
                log.info("Miembro id {} ascendió a nivel OFICIAL", miembro.getId());
            }
        } else if (actual == null) {
            miembro.setNivel(NivelMiembro.APRENDIZ);
        }
    }

    @Override
    @Transactional
    public void otorgarInsigniaOficial(Long actividadId) {
        ActividadAncestralMsp actividad = actividadMspRepository.findById(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad ancestral no encontrada con id: " + actividadId));

        SaberAncestral saber = actividad.getSaberAncestral();
        if (saber == null || saber.getNombreInsigniaOficial() == null || saber.getNombreInsigniaOficial().isBlank()) {
            return;
        }

        if (actividad.getParticipantes() == null) {
            return;
        }

        for (ActividadMspParticipante participante : actividad.getParticipantes()) {
            Miembro miembro = participante.getMiembro();
            if (miembro == null || miembro.getId() == null) {
                continue;
            }

            boolean yaPosee = miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(
                    miembro.getId(), saber.getId());

            if (!yaPosee) {
                MiembroInsignia insignia = new MiembroInsignia();
                insignia.setMiembro(miembro);
                insignia.setComunidad(actividad.getComunidad());
                insignia.setTipoOrigen(TipoOrigenInsignia.OFICIAL_SABER_MSP);
                insignia.setNombreInsignia(saber.getNombreInsigniaOficial());
                insignia.setDescripcion("Insignia oficial obtenida por completar el hito: " + saber.getTitulo());
                insignia.setIconoCodigo("medalla-kinti");
                insignia.setSaberAncestral(saber);
                insignia.setFechaObtencion(LocalDate.now());
                miembroInsigniaRepository.save(insignia);
            }
        }
    }
}
