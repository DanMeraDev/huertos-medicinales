package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ActualizarComunidadRequest;
import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.dto.ComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
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
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.ComunidadServiceImpl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComunidadServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-11-11T15:00:00Z");

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private HuertoRepository huertoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private ComunidadMapper comunidadMapper = Mappers.getMapper(ComunidadMapper.class);

    private ComunidadService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ComunidadServiceImpl(
                comunidadRepository,
                huertoRepository,
                usuarioRepository,
                comunidadMapper,
                Clock.fixed(AHORA, ZoneOffset.UTC)
        );
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

    @Test
    void obtenerPorResponsableIdRetornaDetalleDeComunidad() {
        Long responsableId = 1L;
        Usuario responsable = new Usuario();
        responsable.setId(responsableId);
        responsable.setNombreCompleto("Segundo Tránsito Amaguaña");

        Comunidad comunidad = new Comunidad();
        comunidad.setId(10L);
        comunidad.setNombre("Huerto Allpa");
        comunidad.setResponsable(responsable);

        ComunidadDetalleResponse esperado = new ComunidadDetalleResponse(
                10L,
                "Huerto Allpa",
                TipoOrganizacion.BARRIO,
                "Pichincha",
                "Calderón",
                "Calle Los Pinos",
                "0991234567",
                "comunidad@huertos.gob.ec",
                responsableId,
                "Segundo Tránsito Amaguaña",
                0,
                true,
                LocalDateTime.now()
        );

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        doReturn(esperado).when(comunidadMapper).toDetalleResponse(comunidad);

        ComunidadDetalleResponse resultado = servicio.obtenerPorResponsableId(responsableId);

        assertThat(resultado).isEqualTo(esperado);
        verify(comunidadRepository).findByResponsableId(responsableId);
    }

    @Test
    void obtenerPorResponsableIdLanzaExcepcionCuandoNoExisteComunidad() {
        Long responsableId = 99L;
        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerPorResponsableId(responsableId))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró una comunidad asignada al usuario");
    }

    @Test
    void actualizarInformacionContactoGuardaCambiosExitosamente() {
        Long responsableId = 1L;
        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Huerto Comunitario Allpa Renovado",
                "0997654321",
                "nuevo.correo@huertos.gob.ec"
        );

        Usuario responsable = new Usuario();
        responsable.setId(responsableId);

        Comunidad comunidad = new Comunidad();
        comunidad.setId(10L);
        comunidad.setNombre("Huerto Allpa");
        comunidad.setTelefonoContacto("0991234567");
        comunidad.setCorreoContacto("antiguo@huertos.gob.ec");
        comunidad.setResponsable(responsable);

        ComunidadDetalleResponse responseActualizado = new ComunidadDetalleResponse(
                10L,
                "Huerto Comunitario Allpa Renovado",
                TipoOrganizacion.BARRIO,
                "Pichincha",
                "Calderón",
                "Calle Los Pinos",
                "0997654321",
                "nuevo.correo@huertos.gob.ec",
                responsableId,
                "Segundo Tránsito Amaguaña",
                0,
                true,
                LocalDateTime.now()
        );

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(comunidadRepository.existsByNombreAndIdNot(request.nombre(), 10L)).thenReturn(false);
        when(comunidadRepository.save(comunidad)).thenReturn(comunidad);
        doReturn(responseActualizado).when(comunidadMapper).toDetalleResponse(comunidad);

        ComunidadDetalleResponse resultado = servicio.actualizarInformacionContacto(responsableId, request);

        assertThat(resultado).isEqualTo(responseActualizado);
        assertThat(comunidad.getNombre()).isEqualTo("Huerto Comunitario Allpa Renovado");
        assertThat(comunidad.getTelefonoContacto()).isEqualTo("0997654321");
        assertThat(comunidad.getCorreoContacto()).isEqualTo("nuevo.correo@huertos.gob.ec");
        verify(comunidadRepository).save(comunidad);
    }

    @Test
    void actualizarInformacionContactoLanzaConflictoCuandoNombreYaExisteEnOtraComunidad() {
        Long responsableId = 1L;
        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Nombre Repetido",
                "0997654321",
                "nuevo.correo@huertos.gob.ec"
        );

        Comunidad comunidad = new Comunidad();
        comunidad.setId(10L);
        comunidad.setNombre("Huerto Allpa");

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(comunidadRepository.existsByNombreAndIdNot("Nombre Repetido", 10L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.actualizarInformacionContacto(responsableId, request))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe una comunidad registrada con este nombre");

        verify(comunidadRepository, never()).save(any());
    }

    @Test
    void actualizarInformacionContactoLanzaExcepcionCuandoResponsableNoTieneComunidad() {
        Long responsableId = 55L;
        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Huerto Sin Asignar",
                "0997654321",
                "contacto@huertos.gob.ec"
        );

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.actualizarInformacionContacto(responsableId, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró una comunidad asignada al usuario");

        verify(comunidadRepository, never()).save(any());
    }

    @Test
    void obtenerPorCorreoRetornaDetalleDeComunidad() {
        String correo = "responsable@huertos.gob.ec";
        Long responsableId = 2L;

        Usuario usuario = new Usuario();
        usuario.setId(responsableId);
        usuario.setCorreo(correo);

        Comunidad comunidad = new Comunidad();
        comunidad.setId(20L);
        comunidad.setResponsable(usuario);

        ComunidadDetalleResponse esperado = new ComunidadDetalleResponse(
                20L,
                "Huerto Norte",
                TipoOrganizacion.BARRIO,
                "Pichincha",
                "Calderón",
                "Av. Central",
                "0991112233",
                correo,
                responsableId,
                "Lider Comunitario",
                0,
                true,
                LocalDateTime.now()
        );

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        doReturn(esperado).when(comunidadMapper).toDetalleResponse(comunidad);

        ComunidadDetalleResponse resultado = servicio.obtenerPorCorreo(correo);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void obtenerPorCorreoLanzaExcepcionCuandoNoExisteUsuario() {
        String correo = "desconocido@huertos.gob.ec";
        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerPorCorreo(correo))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró el usuario");
    }

    @Test
    void actualizarPorCorreoActualizaExitosamente() {
        String correo = "responsable@huertos.gob.ec";
        Long responsableId = 3L;

        Usuario usuario = new Usuario();
        usuario.setId(responsableId);
        usuario.setCorreo(correo);

        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Huerto Actualizado",
                "0998887766",
                "nuevo@huertos.gob.ec"
        );

        Comunidad comunidad = new Comunidad();
        comunidad.setId(30L);
        comunidad.setNombre("Huerto Antiguo");
        comunidad.setResponsable(usuario);

        ComunidadDetalleResponse responseActualizado = new ComunidadDetalleResponse(
                30L,
                "Huerto Actualizado",
                TipoOrganizacion.BARRIO,
                "Pichincha",
                "Calderón",
                "Av. Central",
                "0998887766",
                "nuevo@huertos.gob.ec",
                responsableId,
                "Lider",
                0,
                true,
                LocalDateTime.now()
        );

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(comunidadRepository.existsByNombreAndIdNot("Huerto Actualizado", 30L)).thenReturn(false);
        when(comunidadRepository.save(comunidad)).thenReturn(comunidad);
        doReturn(responseActualizado).when(comunidadMapper).toDetalleResponse(comunidad);

        ComunidadDetalleResponse resultado = servicio.actualizarPorCorreo(correo, request);

        assertThat(resultado).isEqualTo(responseActualizado);
        assertThat(comunidad.getNombre()).isEqualTo("Huerto Actualizado");
    }

    @Test
    void actualizarPorCorreoLanzaExcepcionCuandoNoExisteUsuario() {
        String correo = "inexistente@huertos.gob.ec";
        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Huerto",
                "0998887766",
                "nuevo@huertos.gob.ec"
        );

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.actualizarPorCorreo(correo, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró el usuario");
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
