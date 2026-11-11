package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.ComunidadMapper;
import com.huertos.model.Comunidad;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.Huerto;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.TipoHuerto;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.service.impl.ComunidadServiceImpl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComunidadServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-11-11T15:00:00Z");

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private HuertoRepository huertoRepository;

    private ComunidadService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new ComunidadServiceImpl(comunidadRepository, huertoRepository,
                Mappers.getMapper(ComunidadMapper.class), Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void registraLaComunidadConSuResponsableYPuntosEnCero() {
        when(comunidadRepository.save(any(Comunidad.class))).thenAnswer(invocacion -> {
            Comunidad comunidad = invocacion.getArgument(0);
            comunidad.setId(7L);
            return comunidad;
        });

        ComunidadResponse respuesta = servicio.registrarComunidadDesdeSolicitud(solicitud(), responsable(true));

        assertThat(respuesta.id()).isEqualTo(7L);
        assertThat(respuesta.nombre()).isEqualTo("Allpa Viva");
        assertThat(respuesta.tipoOrganizacion()).isEqualTo(TipoOrganizacion.BARRIO);
        assertThat(respuesta.provincia()).isEqualTo("Pichincha");
        assertThat(respuesta.parroquia()).isEqualTo("Calderón");
        assertThat(respuesta.direccion()).isEqualTo("Calle de las Rosas");
        assertThat(respuesta.telefonoContacto()).isEqualTo("0991234567");
        assertThat(respuesta.correoContacto()).isEqualTo("allpa@correo.com");
        assertThat(respuesta.responsableId()).isEqualTo(3L);
        assertThat(respuesta.puntosComunidad()).isZero();
        assertThat(respuesta.activa()).isTrue();
        assertThat(respuesta.creadoEn()).isEqualTo(LocalDateTime.of(2026, 11, 11, 15, 0));
    }

    @Test
    void creaElHuertoBaseConElNombreDeLaComunidadYSinArea() {
        when(comunidadRepository.save(any(Comunidad.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.registrarComunidadDesdeSolicitud(solicitud(), responsable(true));

        ArgumentCaptor<Huerto> huertoGuardado = ArgumentCaptor.forClass(Huerto.class);
        verify(huertoRepository).save(huertoGuardado.capture());
        Huerto huerto = huertoGuardado.getValue();
        assertThat(huerto.getNombreHuerto()).isEqualTo(huerto.getComunidad().getNombre());
        assertThat(huerto.getLatitud()).isEqualByComparingTo("-0.0987123");
        assertThat(huerto.getLongitud()).isEqualByComparingTo("-78.4321456");
        assertThat(huerto.getAltitudMsnm()).isEqualTo(2650);
        assertThat(huerto.getDisenoPrincipal()).isEqualTo(DisenoPrincipal.EN_TIERRA);
        assertThat(huerto.getAreaTotalM2()).isNull();
        assertThat(huerto.getTipoHuerto()).isEqualTo(TipoHuerto.BASICO);
        assertThat(huerto.getPuntosHuerto()).isZero();
        assertThat(huerto.isActivo()).isTrue();
    }

    @Test
    void rechazaUnNombreDeComunidadQueYaExiste() {
        when(comunidadRepository.existsByNombre("Allpa Viva")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarComunidadDesdeSolicitud(solicitud(), responsable(true)))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe una comunidad activa registrada con este nombre");
        verify(comunidadRepository, never()).save(any());
        verify(huertoRepository, never()).save(any());
    }

    @Test
    void rechazaUnResponsableInactivo() {
        assertThatThrownBy(() -> servicio.registrarComunidadDesdeSolicitud(solicitud(), responsable(false)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El usuario asignado como responsable no es válido para liderar la comunidad");
        verify(comunidadRepository, never()).save(any());
    }

    private SolicitudComunidad solicitud() {
        SolicitudComunidad solicitud = new SolicitudComunidad();
        solicitud.setId(12L);
        solicitud.setNombreComunidad("Allpa Viva");
        solicitud.setTipoOrganizacion(TipoOrganizacion.BARRIO);
        solicitud.setProvincia("Pichincha");
        solicitud.setParroquia("Calderón");
        solicitud.setDireccion("Calle de las Rosas");
        solicitud.setLatitud(new BigDecimal("-0.0987123"));
        solicitud.setLongitud(new BigDecimal("-78.4321456"));
        solicitud.setAltitudMsnm(2650);
        solicitud.setDisenoPrincipal(DisenoPrincipal.EN_TIERRA);
        solicitud.setCorreoSolicitante("allpa@correo.com");
        solicitud.setCelularSolicitante("0991234567");
        return solicitud;
    }

    private Usuario responsable(boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setActivo(activo);
        return usuario;
    }
}
