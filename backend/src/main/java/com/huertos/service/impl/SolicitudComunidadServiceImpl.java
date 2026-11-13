package com.huertos.service.impl;

import com.huertos.dto.CredencialesEmailDto;
import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.CrearUsuarioRequest;
import com.huertos.dto.RechazarSolicitudRequest;
import com.huertos.dto.RechazoSolicitudEmailDto;
import com.huertos.dto.ResolucionSolicitudResponse;
import com.huertos.dto.SolicitudComunidadResponse;
import com.huertos.dto.SolicitudDetalleCompletoResponse;
import com.huertos.dto.SolicitudResumenResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.SolicitudComunidadMapper;
import com.huertos.model.EstadoSolicitud;
import com.huertos.model.Rol;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.SolicitudComunidadRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.ComunidadService;
import com.huertos.service.SolicitudComunidadService;
import com.huertos.service.UsuarioService;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudComunidadServiceImpl implements SolicitudComunidadService {

    private static final int EDAD_MINIMA = 18;
    private static final int LONGITUD_CLAVE_TEMPORAL = 16;
    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITOS = "23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final SolicitudComunidadRepository solicitudRepository;
    private final ComunidadRepository comunidadRepository;
    private final Clock clock;
    private final SolicitudComunidadMapper solicitudMapper;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final ComunidadService comunidadService;
    private final ApplicationEventPublisher eventos;

    @Override
    public SolicitudComunidadResponse registrarSolicitud(CrearSolicitudComunidadRequest request) {
        validarMayoriaDeEdad(request.fechaNacimiento());
        validarQueNoExista(request.nombreComunidad(), request.cedulaSolicitante());
        validarCorreoSinCuenta(request.correoSolicitante());
        LocalDateTime ahora = LocalDateTime.now(clock);
        SolicitudComunidad solicitud = solicitudMapper.toSolicitudComunidad(request);
        solicitud.setConsentimientoEn(ahora);
        solicitud.setCreadoEn(ahora);
        return solicitudMapper.toSolicitudComunidadResponse(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResumenResponse> listarPorEstado(EstadoSolicitud estado) {
        return solicitudRepository.findByEstadoOrderByCreadoEnAsc(estado).stream()
                .map(solicitudMapper::toResumenResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudDetalleCompletoResponse obtenerPorId(Long id) {
        return solicitudMapper.toDetalleCompletoResponse(buscarSolicitud(id));
    }

    @Override
    public ResolucionSolicitudResponse aprobarSolicitud(Long id, String correoTecnico) {
        SolicitudComunidad solicitud = buscarPendiente(id);
        String claveTemporal = generarClaveTemporal();
        Usuario responsable = usuarioService.crearUsuarioInterno(cuentaDelResponsable(solicitud, claveTemporal));
        Long comunidadId = comunidadService.registrarComunidadDesdeSolicitud(solicitud, responsable).id();
        resolver(solicitud, EstadoSolicitud.APROBADA, correoTecnico);
        eventos.publishEvent(new CredencialesEmailDto(solicitud.getCorreoSolicitante(),
                solicitud.getNombreSolicitante(), solicitud.getNombreComunidad(), claveTemporal));
        return new ResolucionSolicitudResponse(solicitud.getId(), EstadoSolicitud.APROBADA,
                "Solicitud aprobada exitosamente. Comunidad y cuenta de usuario creadas.", comunidadId,
                responsable.getId());
    }

    @Override
    public ResolucionSolicitudResponse rechazarSolicitud(Long id, RechazarSolicitudRequest request,
            String correoTecnico) {
        SolicitudComunidad solicitud = buscarPendiente(id);
        solicitud.setMotivoRechazo(request.motivoRechazo());
        resolver(solicitud, EstadoSolicitud.RECHAZADA, correoTecnico);
        eventos.publishEvent(new RechazoSolicitudEmailDto(solicitud.getCorreoSolicitante(),
                solicitud.getNombreSolicitante(), solicitud.getNombreComunidad(), request.motivoRechazo()));
        return new ResolucionSolicitudResponse(solicitud.getId(), EstadoSolicitud.RECHAZADA,
                "Solicitud rechazada. Se registró el motivo del rechazo.", null, null);
    }

    private SolicitudComunidad buscarSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud indicada"));
    }

    private SolicitudComunidad buscarPendiente(Long id) {
        SolicitudComunidad solicitud = solicitudRepository.findParaResolverById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud indicada"));
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ConflictoException("Esta solicitud ya ha sido resuelta previamente por otro técnico");
        }
        return solicitud;
    }

    private void resolver(SolicitudComunidad solicitud, EstadoSolicitud estado, String correoTecnico) {
        solicitud.setRevisadoPor(usuarioRepository.findByCorreo(correoTecnico)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el técnico autenticado")));
        solicitud.setEstado(estado);
        solicitud.setFechaRevision(LocalDateTime.now(clock));
        solicitudRepository.save(solicitud);
    }

    private CrearUsuarioRequest cuentaDelResponsable(SolicitudComunidad solicitud, String claveTemporal) {
        return new CrearUsuarioRequest(solicitud.getNombreSolicitante(), solicitud.getCedulaSolicitante(),
                solicitud.getFechaNacimientoSolicitante(), solicitud.getGeneroSolicitante(),
                solicitud.getCorreoSolicitante(), solicitud.getCelularSolicitante(), claveTemporal,
                Rol.RESPONSABLE_COMUNIDAD);
    }

    private String generarClaveTemporal() {
        String todos = MAYUSCULAS + MINUSCULAS + DIGITOS;
        List<Character> caracteres = new ArrayList<>();
        caracteres.add(caracterDe(MAYUSCULAS));
        caracteres.add(caracterDe(MINUSCULAS));
        caracteres.add(caracterDe(DIGITOS));
        while (caracteres.size() < LONGITUD_CLAVE_TEMPORAL) {
            caracteres.add(caracterDe(todos));
        }
        Collections.shuffle(caracteres, ALEATORIO);
        StringBuilder clave = new StringBuilder();
        caracteres.forEach(clave::append);
        return clave.toString();
    }

    private char caracterDe(String alfabeto) {
        return alfabeto.charAt(ALEATORIO.nextInt(alfabeto.length()));
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

    private void validarCorreoSinCuenta(String correo) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("Ya existe una cuenta registrada con el correo electrónico indicado");
        }
    }
}
