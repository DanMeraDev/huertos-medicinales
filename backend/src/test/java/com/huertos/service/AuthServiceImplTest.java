package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.huertos.config.JwtService;
import com.huertos.dto.LoginRequest;
import com.huertos.dto.LoginResponse;
import com.huertos.exception.CredencialesInvalidasException;
import com.huertos.exception.CuentaInactivaException;
import com.huertos.mapper.UsuarioMapper;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.AuthServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String CORREO = "responsable@huertos.gob.ec";
    private static final String CLAVE = "Password123!";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService(
            "clave-de-prueba-con-mas-de-32-caracteres-para-hs256", 1440,
            Clock.fixed(Instant.parse("2026-11-11T15:00:00Z"), ZoneOffset.UTC));

    @Mock
    private UsuarioRepository usuarioRepository;

    private AuthService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new AuthServiceImpl(usuarioRepository, passwordEncoder, jwtService,
                Mappers.getMapper(UsuarioMapper.class));
    }

    @Test
    void iniciaSesionYDevuelveUnTokenConLosDatosDelUsuario() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario(true)));

        LoginResponse respuesta = servicio.iniciarSesion(new LoginRequest(CORREO, CLAVE));

        assertThat(respuesta.tipoToken()).isEqualTo("Bearer");
        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.correo()).isEqualTo(CORREO);
        assertThat(respuesta.nombreCompleto()).isEqualTo("Taita Inti Morales");
        assertThat(respuesta.rol()).isEqualTo(Rol.RESPONSABLE_COMUNIDAD);
        assertThat(respuesta.requiereCambioClave()).isTrue();
        Jwt jwt = jwtService.validar(respuesta.token()).orElseThrow();
        assertThat(jwt.getSubject()).isEqualTo(CORREO);
        assertThat(jwt.getClaimAsString("rol")).isEqualTo("RESPONSABLE_COMUNIDAD");
        assertThat(jwt.<Long>getClaim("id")).isEqualTo(1L);
        assertThat(jwt.<Boolean>getClaim("requiereCambioClave")).isTrue();
    }

    @Test
    void rechazaUnCorreoQueNoExiste() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.iniciarSesion(new LoginRequest(CORREO, CLAVE)))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    @Test
    void rechazaUnaContrasenaIncorrecta() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario(true)));

        assertThatThrownBy(() -> servicio.iniciarSesion(new LoginRequest(CORREO, "OtraClave1!")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    @Test
    void rechazaUnaCuentaInactiva() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario(false)));

        assertThatThrownBy(() -> servicio.iniciarSesion(new LoginRequest(CORREO, CLAVE)))
                .isInstanceOf(CuentaInactivaException.class)
                .hasMessage("Esta cuenta se encuentra inactiva. Comuníquese con la administración del sistema");
    }

    private Usuario usuario(boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreCompleto("Taita Inti Morales");
        usuario.setCorreo(CORREO);
        usuario.setPasswordHash(passwordEncoder.encode(CLAVE));
        usuario.setRol(Rol.RESPONSABLE_COMUNIDAD);
        usuario.setRequiereCambioClave(true);
        usuario.setActivo(activo);
        return usuario;
    }
}
