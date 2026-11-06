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
class AdminInicialRunnerTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-05T12:00:00Z"), ZoneOffset.UTC);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void creaElAdministradorCuandoNoExisteNinguno() {
        when(usuarioRepository.existsByRol(Rol.ADMIN_MSP)).thenReturn(false);

        runner("admin@correo.com", "clave-inicial").run(null);

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        Usuario administrador = guardado.getValue();
        assertThat(administrador.getEmail()).isEqualTo("admin@correo.com");
        assertThat(administrador.getRol()).isEqualTo(Rol.ADMIN_MSP);
        assertThat(administrador.isActivo()).isTrue();
        assertThat(administrador.getCreadoEn()).isEqualTo(LocalDateTime.of(2026, 11, 5, 12, 0));
        assertThat(passwordEncoder.matches("clave-inicial", administrador.getContrasena())).isTrue();
    }

    @Test
    void noCreaOtroCuandoYaHayUnAdministrador() {
        when(usuarioRepository.existsByRol(Rol.ADMIN_MSP)).thenReturn(true);

        runner("admin@correo.com", "clave-inicial").run(null);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noCreaNadaSinLasVariablesDefinidas() {
        runner("", "").run(null);

        verify(usuarioRepository, never()).save(any());
    }

    private AdminInicialRunner runner(String email, String contrasena) {
        return new AdminInicialRunner(usuarioRepository, passwordEncoder, RELOJ, email, contrasena);
    }
}
