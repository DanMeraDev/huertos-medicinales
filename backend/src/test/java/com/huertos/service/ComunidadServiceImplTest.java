package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ActualizarComunidadRequest;
import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.ComunidadMapper;
import com.huertos.model.Comunidad;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.ComunidadServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComunidadServiceImplTest {

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ComunidadMapper comunidadMapper;

    @InjectMocks
    private ComunidadServiceImpl comunidadService;

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
        when(comunidadMapper.toDetalleResponse(comunidad)).thenReturn(esperado);

        ComunidadDetalleResponse resultado = comunidadService.obtenerPorResponsableId(responsableId);

        assertThat(resultado).isEqualTo(esperado);
        verify(comunidadRepository).findByResponsableId(responsableId);
    }

    @Test
    void obtenerPorResponsableIdLanzaExcepcionCuandoNoExisteComunidad() {
        Long responsableId = 99L;
        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comunidadService.obtenerPorResponsableId(responsableId))
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
        when(comunidadMapper.toDetalleResponse(comunidad)).thenReturn(responseActualizado);

        ComunidadDetalleResponse resultado = comunidadService.actualizarInformacionContacto(responsableId, request);

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

        assertThatThrownBy(() -> comunidadService.actualizarInformacionContacto(responsableId, request))
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

        assertThatThrownBy(() -> comunidadService.actualizarInformacionContacto(responsableId, request))
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
        when(comunidadMapper.toDetalleResponse(comunidad)).thenReturn(esperado);

        ComunidadDetalleResponse resultado = comunidadService.obtenerPorCorreo(correo);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void obtenerPorCorreoLanzaExcepcionCuandoNoExisteUsuario() {
        String correo = "desconocido@huertos.gob.ec";
        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comunidadService.obtenerPorCorreo(correo))
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
        when(comunidadMapper.toDetalleResponse(comunidad)).thenReturn(responseActualizado);

        ComunidadDetalleResponse resultado = comunidadService.actualizarPorCorreo(correo, request);

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

        assertThatThrownBy(() -> comunidadService.actualizarPorCorreo(correo, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró el usuario");
    }
}
