package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ComunidadResponse;
import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.RechazarSolicitudRequest;
import com.huertos.dto.ResolucionSolicitudResponse;
import com.huertos.dto.SolicitudDetalleCompletoResponse;
import com.huertos.dto.SolicitudResumenResponse;
import com.huertos.dto.SolicitudComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.SolicitudComunidadMapper;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.EstadoSolicitud;
import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.SolicitudComunidadRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.SolicitudComunidadServiceImpl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SolicitudComunidadServiceImplTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-11T15:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 11, 15, 0);
    private static final String NOMBRE = "Huerto Comunitario Allpa Viva";
    private static final String CEDULA = "1710034065";
    private static final String CORREO_TECNICO = "tecnico.msp@huertos.gob.ec";
    private static final String MOTIVO =
            "La ubicación indicada no cuenta con acceso seguro a fuente de agua para el riego comunitario.";
    private static final String MENSAJE_CONFLICTO =
            "Ya existe una solicitud pendiente o una comunidad registrada con el nombre o la cédula indicada";

    @Mock
    private SolicitudComunidadRepository solicitudRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ComunidadService comunidadService;

    private SolicitudComunidadService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new SolicitudComunidadServiceImpl(solicitudRepository, comunidadRepository, RELOJ,
                Mappers.getMapper(SolicitudComunidadMapper.class), usuarioRepository, usuarioService, comunidadService);
    }

    @Test
    void registraLaSolicitudPendienteConElConsentimientoFechado() {
        when(solicitudRepository.save(any(SolicitudComunidad.class))).thenAnswer(invocacion -> {
            SolicitudComunidad guardada = invocacion.getArgument(0);
            guardada.setId(12L);
            return guardada;
        });

        SolicitudComunidadResponse respuesta = servicio.registrarSolicitud(solicitud(LocalDate.of(1990, 5, 14)));

        assertThat(respuesta.id()).isEqualTo(12L);
        assertThat(respuesta.nombreComunidad()).isEqualTo(NOMBRE);
        assertThat(respuesta.estado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(respuesta.creadoEn()).isEqualTo(AHORA);
        verify(solicitudRepository).save(argThat(guardada ->
                guardada.isConsentimientoDatos()
                        && AHORA.equals(guardada.getConsentimientoEn())
                        && LocalDate.of(1990, 5, 14).equals(guardada.getFechaNacimientoSolicitante())
                        && guardada.getRevisadoPor() == null
                        && guardada.getMotivoRechazo() == null));
    }

    @Test
    void rechazaAUnSolicitanteMenorDeEdad() {
        assertThatThrownBy(() -> servicio.registrarSolicitud(solicitud(LocalDate.of(2008, 11, 12))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El solicitante debe ser mayor de 18 años para postular una comunidad");
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void rechazaUnNombreConSolicitudPendiente() {
        when(solicitudRepository.existsByNombreComunidadAndEstado(NOMBRE, EstadoSolicitud.PENDIENTE)).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarSolicitud(solicitud(LocalDate.of(1990, 5, 14))))
                .isInstanceOf(ConflictoException.class)
                .hasMessage(MENSAJE_CONFLICTO);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void rechazaUnaCedulaConSolicitudPendiente() {
        when(solicitudRepository.existsByCedulaSolicitanteAndEstado(CEDULA, EstadoSolicitud.PENDIENTE)).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarSolicitud(solicitud(LocalDate.of(1990, 5, 14))))
                .isInstanceOf(ConflictoException.class)
                .hasMessage(MENSAJE_CONFLICTO);
    }

    @Test
    void rechazaElNombreDeUnaComunidadYaRegistrada() {
        when(comunidadRepository.existsByNombre(NOMBRE)).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarSolicitud(solicitud(LocalDate.of(1990, 5, 14))))
                .isInstanceOf(ConflictoException.class)
                .hasMessage(MENSAJE_CONFLICTO);
    }

    @Test
    void rechazaAQuienYaEsResponsableDeUnaComunidad() {
        when(comunidadRepository.existsByResponsableIdentificacion(CEDULA)).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarSolicitud(solicitud(LocalDate.of(1990, 5, 14))))
                .isInstanceOf(ConflictoException.class)
                .hasMessage(MENSAJE_CONFLICTO);
    }

    @Test
    void listaLasSolicitudesPendientesComoResumen() {
        when(solicitudRepository.findByEstadoOrderByCreadoEnAsc(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitudGuardada(EstadoSolicitud.PENDIENTE)));

        List<SolicitudResumenResponse> pendientes = servicio.listarPorEstado(EstadoSolicitud.PENDIENTE);

        assertThat(pendientes).containsExactly(new SolicitudResumenResponse(12L, "Segundo Tránsito Amaguaña",
                LocalDate.of(2026, 11, 10), "Pichincha"));
    }

    @Test
    void devuelveLaFichaCompletaDeLaSolicitud() {
        when(solicitudRepository.findById(12L)).thenReturn(Optional.of(solicitudGuardada(EstadoSolicitud.PENDIENTE)));

        SolicitudDetalleCompletoResponse detalle = servicio.obtenerPorId(12L);

        assertThat(detalle.nombreComunidad()).isEqualTo(NOMBRE);
        assertThat(detalle.cedulaSolicitante()).isEqualTo(CEDULA);
        assertThat(detalle.fechaNacimientoSolicitante()).isEqualTo(LocalDate.of(1990, 5, 14));
        assertThat(detalle.disenoPrincipal()).isEqualTo(DisenoPrincipal.EN_TIERRA);
    }

    @Test
    void apruebaCreandoLaCuentaDelResponsableYLaComunidad() {
        SolicitudComunidad solicitud = solicitudGuardada(EstadoSolicitud.PENDIENTE);
        Usuario tecnico = new Usuario();
        Usuario responsable = new Usuario();
        responsable.setId(8L);
        when(solicitudRepository.findById(12L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findByCorreo(CORREO_TECNICO)).thenReturn(Optional.of(tecnico));
        when(usuarioService.crearUsuarioInterno(any())).thenReturn(responsable);
        when(comunidadService.registrarComunidadDesdeSolicitud(solicitud, responsable)).thenReturn(comunidadCreada());

        ResolucionSolicitudResponse respuesta = servicio.aprobarSolicitud(12L, CORREO_TECNICO);

        assertThat(respuesta).isEqualTo(new ResolucionSolicitudResponse(12L, EstadoSolicitud.APROBADA,
                "Solicitud aprobada exitosamente. Comunidad y cuenta de usuario creadas.", 3L, 8L));
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.APROBADA);
        assertThat(solicitud.getRevisadoPor()).isSameAs(tecnico);
        assertThat(solicitud.getFechaRevision()).isEqualTo(AHORA);
        verify(usuarioService).crearUsuarioInterno(argThat(cuenta ->
                CEDULA.equals(cuenta.identificacion())
                        && LocalDate.of(1990, 5, 14).equals(cuenta.fechaNacimiento())
                        && "comunidad.allpa@huertos.gob.ec".equals(cuenta.correo())
                        && cuenta.rol() == Rol.RESPONSABLE_COMUNIDAD
                        && cuenta.password().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{12,}$")));
    }

    @Test
    void rechazaGuardandoElMotivoYElTecnico() {
        SolicitudComunidad solicitud = solicitudGuardada(EstadoSolicitud.PENDIENTE);
        Usuario tecnico = new Usuario();
        when(solicitudRepository.findById(12L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findByCorreo(CORREO_TECNICO)).thenReturn(Optional.of(tecnico));

        ResolucionSolicitudResponse respuesta = servicio.rechazarSolicitud(12L,
                new RechazarSolicitudRequest(MOTIVO), CORREO_TECNICO);

        assertThat(respuesta).isEqualTo(new ResolucionSolicitudResponse(12L, EstadoSolicitud.RECHAZADA,
                "Solicitud rechazada. Se registró el motivo del rechazo.", null, null));
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADA);
        assertThat(solicitud.getMotivoRechazo()).isEqualTo(MOTIVO);
        assertThat(solicitud.getRevisadoPor()).isSameAs(tecnico);
        assertThat(solicitud.getFechaRevision()).isEqualTo(AHORA);
        verify(usuarioService, never()).crearUsuarioInterno(any());
    }

    @Test
    void noResuelveUnaSolicitudYaResuelta() {
        when(solicitudRepository.findById(12L)).thenReturn(Optional.of(solicitudGuardada(EstadoSolicitud.RECHAZADA)));

        assertThatThrownBy(() -> servicio.aprobarSolicitud(12L, CORREO_TECNICO))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Esta solicitud ya ha sido resuelta previamente por otro técnico");
        verify(usuarioService, never()).crearUsuarioInterno(any());
    }

    @Test
    void avisaCuandoLaSolicitudNoExiste() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.rechazarSolicitud(99L, new RechazarSolicitudRequest(MOTIVO), CORREO_TECNICO))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe la solicitud indicada");
    }

    private SolicitudComunidad solicitudGuardada(EstadoSolicitud estado) {
        SolicitudComunidad solicitud = Mappers.getMapper(SolicitudComunidadMapper.class)
                .toSolicitudComunidad(solicitud(LocalDate.of(1990, 5, 14)));
        solicitud.setId(12L);
        solicitud.setEstado(estado);
        solicitud.setCreadoEn(LocalDateTime.of(2026, 11, 10, 11, 0));
        return solicitud;
    }

    private ComunidadResponse comunidadCreada() {
        return new ComunidadResponse(3L, NOMBRE, TipoOrganizacion.BARRIO, "Pichincha", "Calderon",
                "Calle de las Rosas y Pasaje Los Pinos, lote 14", "0991234567", "comunidad.allpa@huertos.gob.ec",
                8L, 0, true, AHORA);
    }

    private CrearSolicitudComunidadRequest solicitud(LocalDate fechaNacimiento) {
        return new CrearSolicitudComunidadRequest(NOMBRE, TipoOrganizacion.BARRIO, "Pichincha", "Calderon",
                "Calle de las Rosas y Pasaje Los Pinos, lote 14", new BigDecimal("-0.0987123"),
                new BigDecimal("-78.4321456"), 2650, DisenoPrincipal.EN_TIERRA, "SURCOS_WACHU,TERRAZAS_ANTI",
                CEDULA, "Segundo Tránsito Amaguaña", fechaNacimiento, Genero.MASCULINO,
                "comunidad.allpa@huertos.gob.ec", "0991234567", true);
    }
}
