package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ActualizarRolRequest;
import com.huertos.dto.UsuarioResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.UsuarioMapper;
import com.huertos.model.AuditoriaRol;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.AuditoriaRolRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.AdminUsuarioServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUsuarioServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T12:00:00Z");
    private static final String EMAIL_ADMIN = "admin@correo.com";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaRolRepository auditoriaRolRepository;

    private AdminUsuarioService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new AdminUsuarioServiceImpl(
                usuarioRepository,
                auditoriaRolRepository,
                Mappers.getMapper(UsuarioMapper.class),
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void listaLosUsuariosComoRespuestas() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario(2L, Rol.MIEMBRO, true)));

        List<UsuarioResponse> usuarios = servicio.listarUsuarios();

        assertThat(usuarios).extracting(UsuarioResponse::id).containsExactly(2L);
        assertThat(usuarios.getFirst().activo()).isTrue();
    }

    @Test
    void cambiaElRolYRegistraLaAuditoria() {
        Usuario usuario = usuario(2L, Rol.MIEMBRO, true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail(EMAIL_ADMIN)).thenReturn(Optional.of(usuario(1L, Rol.ADMIN_MSP, true)));

        UsuarioResponse respuesta = servicio.actualizarRol(
                2L, new ActualizarRolRequest(Rol.RESPONSABLE_COMUNIDAD), EMAIL_ADMIN);

        assertThat(respuesta.rol()).isEqualTo(Rol.RESPONSABLE_COMUNIDAD);
        assertThat(usuario.getRol()).isEqualTo(Rol.RESPONSABLE_COMUNIDAD);
        ArgumentCaptor<AuditoriaRol> auditoria = ArgumentCaptor.forClass(AuditoriaRol.class);
        verify(auditoriaRolRepository).save(auditoria.capture());
        assertThat(auditoria.getValue().getUsuarioId()).isEqualTo(2L);
        assertThat(auditoria.getValue().getRolAnterior()).isEqualTo(Rol.MIEMBRO);
        assertThat(auditoria.getValue().getRolNuevo()).isEqualTo(Rol.RESPONSABLE_COMUNIDAD);
        assertThat(auditoria.getValue().getModificadoPor()).isEqualTo(1L);
        assertThat(auditoria.getValue().getFechaCambio()).isEqualTo(LocalDateTime.of(2026, 11, 5, 12, 0));
    }

    @Test
    void rechazaUnUsuarioInexistente() {
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.actualizarRol(9L, new ActualizarRolRequest(Rol.MIEMBRO), EMAIL_ADMIN))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe un usuario con el ID especificado.");
    }

    @Test
    void rechazaDegradarAlUnicoAdministradorActivo() {
        Usuario administrador = usuario(1L, Rol.ADMIN_MSP, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN_MSP)).thenReturn(1L);

        assertThatThrownBy(() -> servicio.actualizarRol(1L, new ActualizarRolRequest(Rol.MIEMBRO), EMAIL_ADMIN))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("No es posible cambiar el rol del único administrador activo del sistema.");
        assertThat(administrador.getRol()).isEqualTo(Rol.ADMIN_MSP);
        verify(auditoriaRolRepository, never()).save(any());
    }

    @Test
    void permiteDegradarAUnAdministradorSiQuedaOtroActivo() {
        Usuario administrador = usuario(3L, Rol.ADMIN_MSP, true);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(administrador));
        when(usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN_MSP)).thenReturn(2L);
        when(usuarioRepository.findByEmail(EMAIL_ADMIN)).thenReturn(Optional.of(usuario(1L, Rol.ADMIN_MSP, true)));

        servicio.actualizarRol(3L, new ActualizarRolRequest(Rol.MIEMBRO), EMAIL_ADMIN);

        assertThat(administrador.getRol()).isEqualTo(Rol.MIEMBRO);
    }

    @Test
    void permiteCambiarUnAdministradorInactivoAunqueSoloQuedeUnoActivo() {
        Usuario inactivo = usuario(3L, Rol.ADMIN_MSP, false);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(inactivo));
        when(usuarioRepository.findByEmail(EMAIL_ADMIN)).thenReturn(Optional.of(usuario(1L, Rol.ADMIN_MSP, true)));

        servicio.actualizarRol(3L, new ActualizarRolRequest(Rol.MIEMBRO), EMAIL_ADMIN);

        assertThat(inactivo.getRol()).isEqualTo(Rol.MIEMBRO);
    }

    private Usuario usuario(Long id, Rol rol, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        return usuario;
    }
}
