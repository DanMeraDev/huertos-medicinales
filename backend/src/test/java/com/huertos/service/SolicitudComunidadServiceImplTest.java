package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.SolicitudComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.SolicitudComunidadMapper;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.EstadoSolicitud;
import com.huertos.model.Genero;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.TipoOrganizacion;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.SolicitudComunidadRepository;
import com.huertos.service.impl.SolicitudComunidadServiceImpl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private static final String MENSAJE_CONFLICTO =
            "Ya existe una solicitud pendiente o una comunidad registrada con el nombre o la cédula indicada";

    @Mock
    private SolicitudComunidadRepository solicitudRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    private SolicitudComunidadService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new SolicitudComunidadServiceImpl(solicitudRepository, comunidadRepository, RELOJ,
                Mappers.getMapper(SolicitudComunidadMapper.class));
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

    private CrearSolicitudComunidadRequest solicitud(LocalDate fechaNacimiento) {
        return new CrearSolicitudComunidadRequest(NOMBRE, TipoOrganizacion.BARRIO, "Pichincha", "Calderon",
                "Calle de las Rosas y Pasaje Los Pinos, lote 14", new BigDecimal("-0.0987123"),
                new BigDecimal("-78.4321456"), 2650, DisenoPrincipal.EN_TIERRA, "SURCOS_WACHU,TERRAZAS_ANTI",
                CEDULA, "Segundo Tránsito Amaguaña", fechaNacimiento, Genero.MASCULINO,
                "comunidad.allpa@huertos.gob.ec", "0991234567", true);
    }
}
