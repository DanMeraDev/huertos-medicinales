package com.huertos.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TecnicoSisInicialRunnerTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-05T12:00:00Z"), ZoneOffset.UTC);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void creaElTecnicoDeSistemasCuandoNoExisteNinguno() {
        when(usuarioRepository.existsByRol(Rol.TECNICO_SIS)).thenReturn(false);

        runner("admin@correo.com", "clave-inicial").run(null);

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        Usuario tecnico = guardado.getValue();
        assertThat(tecnico.getEmail()).isEqualTo("admin@correo.com");
        assertThat(tecnico.getRol()).isEqualTo(Rol.TECNICO_SIS);
        assertThat(tecnico.isActivo()).isTrue();
        assertThat(tecnico.isRequiereCambioClave()).isFalse();
        assertThat(tecnico.getCreadoEn()).isEqualTo(LocalDateTime.of(2026, 11, 5, 12, 0));
        assertThat(passwordEncoder.matches("clave-inicial", tecnico.getContrasena())).isTrue();
    }

    @Test
    void noCreaOtroCuandoYaHayUnTecnicoDeSistemas() {
        when(usuarioRepository.existsByRol(Rol.TECNICO_SIS)).thenReturn(true);

        runner("admin@correo.com", "clave-inicial").run(null);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noCreaNadaSinLasVariablesDefinidas() {
        runner("", "").run(null);

        verify(usuarioRepository, never()).save(any());
    }

    private TecnicoSisInicialRunner runner(String email, String contrasena) {
        return new TecnicoSisInicialRunner(usuarioRepository, passwordEncoder, RELOJ, email, contrasena);
    }
}
