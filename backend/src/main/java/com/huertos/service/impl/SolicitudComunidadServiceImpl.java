package com.huertos.service.impl;

import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.SolicitudComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.SolicitudComunidadMapper;
import com.huertos.model.EstadoSolicitud;
import com.huertos.model.SolicitudComunidad;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.SolicitudComunidadRepository;
import com.huertos.service.SolicitudComunidadService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudComunidadServiceImpl implements SolicitudComunidadService {

    private static final int EDAD_MINIMA = 18;

    private final SolicitudComunidadRepository solicitudRepository;
    private final ComunidadRepository comunidadRepository;
    private final Clock clock;
    private final SolicitudComunidadMapper solicitudMapper;

    @Override
    public SolicitudComunidadResponse registrarSolicitud(CrearSolicitudComunidadRequest request) {
        validarMayoriaDeEdad(request.fechaNacimiento());
        validarQueNoExista(request.nombreComunidad(), request.cedulaSolicitante());
        LocalDateTime ahora = LocalDateTime.now(clock);
        SolicitudComunidad solicitud = solicitudMapper.toSolicitudComunidad(request);
        solicitud.setConsentimientoEn(ahora);
        solicitud.setCreadoEn(ahora);
        return solicitudMapper.toSolicitudComunidadResponse(solicitudRepository.save(solicitud));
    }

    private void validarMayoriaDeEdad(LocalDate fechaNacimiento) {
        if (Period.between(fechaNacimiento, LocalDate.now(clock)).getYears() < EDAD_MINIMA) {
            throw new ReglaNegocioException("El solicitante debe ser mayor de 18 años para postular una comunidad");
        }
    }

    private void validarQueNoExista(String nombreComunidad, String cedula) {
        if (solicitudRepository.existsByNombreComunidadAndEstado(nombreComunidad, EstadoSolicitud.PENDIENTE)
                || solicitudRepository.existsByCedulaSolicitanteAndEstado(cedula, EstadoSolicitud.PENDIENTE)
                || comunidadRepository.existsByNombre(nombreComunidad)
                || comunidadRepository.existsByResponsableIdentificacion(cedula)) {
            throw new ConflictoException(
                    "Ya existe una solicitud pendiente o una comunidad registrada con el nombre o la cédula indicada");
        }
    }
}
