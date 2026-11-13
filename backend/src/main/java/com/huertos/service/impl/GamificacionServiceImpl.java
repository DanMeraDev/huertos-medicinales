package com.huertos.service.impl;

import com.huertos.config.ConstantesGamificacion;
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
import com.huertos.repository.RegistroActividadMiembroRepository;
import com.huertos.repository.TalentoComunidadRepository;
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

    private static final int PUNTOS_BASE_DEFECTO = 20;
    private static final int PUNTOS_POR_HORA = 1;
    private static final float FACTOR_PENALIZACION_INTENTO_DOS = 0.70f;

    private final ComunidadRepository comunidadRepository;
    private final HuertoRepository huertoRepository;
    private final MiembroRepository miembroRepository;
    private final ActividadAncestralMspRepository actividadMspRepository;
    private final MiembroInsigniaRepository miembroInsigniaRepository;
    private final RegistroActividadMiembroRepository registroActividadMiembroRepository;
    private final TalentoComunidadRepository talentoComunidadRepository;

    @Override
    @Transactional
    public void procesarActividadCotidiana(Long registroActividadId) {
        // Stub inicial para TDD
    }

    @Override
    @Transactional
    public void procesarPuntosActividadMsp(Long actividadId) {
        ActividadAncestralMsp actividad = obtenerYValidarActividad(actividadId);

        int puntosComunidad = calcularPuntosComunidad(actividad);
        actividad.setPuntosAcreditarComunidad(puntosComunidad);

        actualizarPuntosComunidadYHuerto(actividad, puntosComunidad);
        procesarPuntosParticipantes(actividad);
        otorgarInsigniaOficial(actividadId);

        actividadMspRepository.save(actividad);
    }

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

        adjudicarInsigniasAParticipantes(actividad, saber);
    }

    private void adjudicarInsigniasAParticipantes(ActividadAncestralMsp actividad, SaberAncestral saber) {
        for (ActividadMspParticipante participante : actividad.getParticipantes()) {
            Miembro miembro = participante.getMiembro();
            if (miembro != null && miembro.getId() != null) {
                adjudicarInsigniaAMiembroSiNoLaPosee(miembro, actividad.getComunidad(), saber);
            }
        }
    }

    private void adjudicarInsigniaAMiembroSiNoLaPosee(Miembro miembro, Comunidad comunidad, SaberAncestral saber) {
        boolean yaPosee = miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(
                miembro.getId(), saber.getId());

        if (!yaPosee) {
            MiembroInsignia insignia = construirInsigniaOficial(miembro, comunidad, saber);
            miembroInsigniaRepository.save(insignia);
            log.info("Insignia oficial '{}' otorgada al miembro id {} por saber ancestral id {}",
                    saber.getNombreInsigniaOficial(), miembro.getId(), saber.getId());
        }
    }

    private ActividadAncestralMsp obtenerYValidarActividad(Long actividadId) {
        ActividadAncestralMsp actividad = actividadMspRepository.findById(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad ancestral no encontrada con id: " + actividadId));

        if (actividad.getEstadoVerificacion() != EstadoVerificacion.APROBADA) {
            throw new ReglaNegocioException("No se pueden otorgar puntos de gamificación en actividades no aprobadas");
        }
        return actividad;
    }

    private int calcularPuntosComunidad(ActividadAncestralMsp actividad) {
        int puntosBase = actividad.getSaberAncestral() != null
                ? actividad.getSaberAncestral().getPuntosBaseComunidad()
                : PUNTOS_BASE_DEFECTO;

        if (actividad.getNumeroIntento() == 1) {
            return puntosBase;
        }
        return Math.round(puntosBase * FACTOR_PENALIZACION_INTENTO_DOS);
    }

    private void actualizarPuntosComunidadYHuerto(ActividadAncestralMsp actividad, int puntosComunidad) {
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
    }

    private void procesarPuntosParticipantes(ActividadAncestralMsp actividad) {
        if (actividad.getParticipantes() == null) {
            return;
        }

        for (ActividadMspParticipante participante : actividad.getParticipantes()) {
            int puntosVoluntario = participante.getHorasTrabajadas() * PUNTOS_POR_HORA;
            participante.setPuntosAcreditados(puntosVoluntario);

            Miembro miembro = participante.getMiembro();
            if (miembro != null) {
                acreditarPuntosAMiembro(miembro, puntosVoluntario);
            }
        }
    }

    private void acreditarPuntosAMiembro(Miembro miembro, int puntosVoluntario) {
        Miembro miembroPersistido = miembro.getId() != null
                ? miembroRepository.findById(miembro.getId()).orElse(miembro)
                : miembro;

        miembroPersistido.setPuntosTotales(miembroPersistido.getPuntosTotales() + puntosVoluntario);
        NivelMiembro nivelAnterior = miembroPersistido.getNivel();
        NivelMiembro nuevoNivel = determinarNuevoNivel(miembroPersistido.getPuntosTotales(), nivelAnterior);
        if (nuevoNivel != nivelAnterior) {
            miembroPersistido.setNivel(nuevoNivel);
            log.info("Miembro id {} ascendió a nivel {}", miembroPersistido.getId(), nuevoNivel);
        }
        miembroRepository.save(miembroPersistido);

        if (!miembroPersistido.isActivo()) {
            log.info("Miembro inactivo id {} acumuló {} puntos sin notificaciones activas.",
                    miembroPersistido.getId(), puntosVoluntario);
        }
    }

    private MiembroInsignia construirInsigniaOficial(Miembro miembro, Comunidad comunidad, SaberAncestral saber) {
        MiembroInsignia insignia = new MiembroInsignia();
        insignia.setMiembro(miembro);
        insignia.setComunidad(comunidad);
        insignia.setTipoOrigen(TipoOrigenInsignia.OFICIAL_SABER_MSP);
        insignia.setNombreInsignia(saber.getNombreInsigniaOficial());
        insignia.setDescripcion(ConstantesGamificacion.PREFIJO_DESCRIPCION_INSIGNIA + saber.getTitulo());
        insignia.setIconoCodigo(ConstantesGamificacion.ICONO_INSIGNIA_OFICIAL);
        insignia.setSaberAncestral(saber);
        insignia.setFechaObtencion(LocalDate.now());
        return insignia;
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
