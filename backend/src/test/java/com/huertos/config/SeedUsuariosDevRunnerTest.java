package com.huertos.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.model.Comunidad;
import com.huertos.model.Huerto;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SeedUsuariosDevRunnerTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private HuertoRepository huertoRepository;

    @Test
    void creaCuentasDeCadaRolYComunidadConHuertoCuandoNoExisten() {
        Usuario responsableGuardado = new Usuario();
        responsableGuardado.setId(3L);
        responsableGuardado.setCorreo("responsable.comunidad@huertos.gob.ec");

        when(usuarioRepository.existsByCorreo(any())).thenReturn(false);
        when(usuarioRepository.existsByIdentificacion(any())).thenReturn(false);
        when(usuarioRepository.findByCorreo("responsable.comunidad@huertos.gob.ec"))
                .thenReturn(Optional.of(responsableGuardado));
        when(comunidadRepository.findByResponsableId(3L)).thenReturn(Optional.empty());
        when(comunidadRepository.existsByNombre("Comunidad San Pedro de Pruebas")).thenReturn(false);
        when(comunidadRepository.save(any(Comunidad.class))).thenAnswer(inv -> inv.getArgument(0));

        runner().run(null);

        ArgumentCaptor<Usuario> usuariosCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(3)).save(usuariosCaptor.capture());

        List<Usuario> usuarios = usuariosCaptor.getAllValues();
        assertThat(usuarios).extracting(Usuario::getRol)
                .containsExactlyInAnyOrder(Rol.TECNICO_SIS, Rol.TECNICO_MSP, Rol.RESPONSABLE_COMUNIDAD);
        assertThat(usuarios).allSatisfy(usuario -> {
            assertThat(usuario.isActivo()).isTrue();
            assertThat(usuario.isRequiereCambioClave()).isFalse();
            assertThat(passwordEncoder.matches("Pruebas123!", usuario.getPasswordHash())).isTrue();
        });

        ArgumentCaptor<Comunidad> comunidadCaptor = ArgumentCaptor.forClass(Comunidad.class);
        verify(comunidadRepository).save(comunidadCaptor.capture());
        assertThat(comunidadCaptor.getValue().getResponsable()).isEqualTo(responsableGuardado);

        ArgumentCaptor<Huerto> huertoCaptor = ArgumentCaptor.forClass(Huerto.class);
        verify(huertoRepository).save(huertoCaptor.capture());
        assertThat(huertoCaptor.getValue().getComunidad()).isEqualTo(comunidadCaptor.getValue());
    }

    @Test
    void noDuplicaRegistrosCuandoYaExisten() {
        Usuario responsableExistente = new Usuario();
        responsableExistente.setId(3L);

        when(usuarioRepository.existsByCorreo(any())).thenReturn(true);
        when(usuarioRepository.findByCorreo("responsable.comunidad@huertos.gob.ec"))
                .thenReturn(Optional.of(responsableExistente));
        when(comunidadRepository.findByResponsableId(3L)).thenReturn(Optional.of(new Comunidad()));

        runner().run(null);

        verify(usuarioRepository, never()).save(any());
        verify(comunidadRepository, never()).save(any());
        verify(huertoRepository, never()).save(any());
    }

    private SeedUsuariosDevRunner runner() {
        return new SeedUsuariosDevRunner(
                usuarioRepository, comunidadRepository, huertoRepository, passwordEncoder, RELOJ);
    }
}

