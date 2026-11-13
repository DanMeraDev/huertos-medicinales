package com.huertos.service.impl;

import com.huertos.dto.ActividadRevisionResponse;
import com.huertos.dto.ResultadoVerificacionResponse;
import com.huertos.dto.VerificarActividadMspRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.ActividadAncestralMapper;
import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.Usuario;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.GamificacionService;
import com.huertos.service.MspRevisionService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MspRevisionServiceImpl implements MspRevisionService {

    private final ActividadAncestralMspRepository actividadAncestralRepository;
    private final UsuarioRepository usuarioRepository;
    private final GamificacionService gamificacionService;
    private final ActividadAncestralMapper actividadAncestralMapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<ActividadRevisionResponse> listarPendientes() {
        return actividadAncestralRepository
                .findByEstadoVerificacionOrderByCreadoEnAsc(EstadoVerificacion.PENDIENTE)
                .stream()
                .map(actividadAncestralMapper::toActividadRevisionResponse)
                .toList();
    }

    @Override
    @Transactional
    public ResultadoVerificacionResponse dictaminarActividad(String correoTecnico, Long actividadId,
            VerificarActividadMspRequest request) {
        ActividadAncestralMsp actividad = buscarActividadPendiente(actividadId);
        validarDictamen(request);
        validarObservacion(request);

        Usuario tecnico = usuarioRepository.findByCorreo(correoTecnico)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el técnico autenticado"));

        LocalDateTime fechaVerificacion = LocalDateTime.now(clock);
        actividad.setEstadoVerificacion(request.nuevoEstado());
        actividad.setObservacionMsp(request.observacion());
        actividad.setVerificadoPor(tecnico);
        actividad.setFechaVerificacion(fechaVerificacion);
        actividadAncestralRepository.save(actividad);

        int puntosAcreditados = acreditarPuntosSiAprobada(actividad, request.nuevoEstado());

        return new ResultadoVerificacionResponse(actividadId, request.nuevoEstado(), puntosAcreditados,
                fechaVerificacion);
    }

    private ActividadAncestralMsp buscarActividadPendiente(Long actividadId) {
        ActividadAncestralMsp actividad = actividadAncestralRepository.findParaDictaminarById(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La actividad ancestral no existe"));

        if (actividad.getEstadoVerificacion() != EstadoVerificacion.PENDIENTE) {
            throw new ConflictoException("La actividad ancestral ya fue dictaminada y no se encuentra en estado pendiente");
        }
        return actividad;
    }

    private void validarDictamen(VerificarActividadMspRequest request) {
        if (request.nuevoEstado() == null || request.nuevoEstado() == EstadoVerificacion.PENDIENTE) {
            throw new ReglaNegocioException(
                    "El dictamen debe ser APROBADA, OBSERVADA_CORREGIR o RECHAZADA_DEFINITIVA");
        }
    }

    private void validarObservacion(VerificarActividadMspRequest request) {
        boolean requiereObservacion = request.nuevoEstado() == EstadoVerificacion.OBSERVADA_CORREGIR
                || request.nuevoEstado() == EstadoVerificacion.RECHAZADA_DEFINITIVA;

        if (requiereObservacion && (request.observacion() == null || request.observacion().isBlank())) {
            throw new ReglaNegocioException(
                    "Debe ingresar una observación detallada para justificar la observación o rechazo");
        }
    }

    private int acreditarPuntosSiAprobada(ActividadAncestralMsp actividad, EstadoVerificacion estado) {
        if (estado != EstadoVerificacion.APROBADA) {
            return 0;
        }

        gamificacionService.procesarPuntosActividadMsp(actividad.getId());

        return actividadAncestralRepository.findById(actividad.getId())
                .map(ActividadAncestralMsp::getPuntosAcreditarComunidad)
                .orElse(0);
    }
}
