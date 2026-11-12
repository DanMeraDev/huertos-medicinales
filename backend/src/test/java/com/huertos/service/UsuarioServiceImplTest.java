package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.CrearUsuarioRequest;
import com.huertos.dto.UsuarioDetalleResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.UsuarioMapper;
import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.UsuarioServiceImpl;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final String CLAVE = "PasswordSegura2026";
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-11T15:00:00Z"), ZoneOffset.UTC);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, RELOJ,
                Mappers.getMapper(UsuarioMapper.class));
    }

    @Test
    void creaElUsuarioActivoConLaClaveCifradaYCambioDeClavePendiente() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario guardado = invocacion.getArgument(0);
            guardado.setId(5L);
            return guardado;
        });

        UsuarioDetalleResponse respuesta = servicio.crearUsuario(solicitud(LocalDate.of(1990, 5, 14)));

        assertThat(respuesta.id()).isEqualTo(5L);
        assertThat(respuesta.identificacion()).isEqualTo("1710034065");
        assertThat(respuesta.correo()).isEqualTo("rosa.chimbolema@huertos.gob.ec");
        assertThat(respuesta.rol()).isEqualTo(Rol.TECNICO_MSP);
        assertThat(respuesta.genero()).isEqualTo(Genero.FEMENINO);
        assertThat(respuesta.activo()).isTrue();
        assertThat(respuesta.requiereCambioClave()).isTrue();
        assertThat(respuesta.creadoEn()).isEqualTo(LocalDateTime.of(2026, 11, 11, 15, 0));
        verify(usuarioRepository).save(argThat(usuario ->
                passwordEncoder.matches(CLAVE, usuario.getPasswordHash())));
    }

    @Test
    void aceptaAQuienCumpleDieciochoAniosHoy() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        UsuarioDetalleResponse respuesta = servicio.crearUsuario(solicitud(LocalDate.of(2008, 11, 11)));

        assertThat(respuesta.fechaNacimiento()).isEqualTo(LocalDate.of(2008, 11, 11));
    }

    @Test
    void rechazaAUnMenorDeEdad() {
        assertThatThrownBy(() -> servicio.crearUsuario(solicitud(LocalDate.of(2008, 11, 12))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El usuario debe ser mayor de 18 años para ser registrado en la plataforma");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaUnaCedulaYaRegistrada() {
        when(usuarioRepository.existsByIdentificacion("1710034065")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crearUsuario(solicitud(LocalDate.of(1990, 5, 14))))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un usuario registrado con la cédula ingresada");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaUnCorreoYaRegistrado() {
        when(usuarioRepository.existsByCorreo("rosa.chimbolema@huertos.gob.ec")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crearUsuario(solicitud(LocalDate.of(1990, 5, 14))))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un usuario registrado con el correo electrónico ingresado");
        verify(usuarioRepository, never()).save(any());
    }

    private CrearUsuarioRequest solicitud(LocalDate fechaNacimiento) {
        return new CrearUsuarioRequest("Rosa Elena Chimbolema", "1710034065", fechaNacimiento, Genero.FEMENINO,
                "rosa.chimbolema@huertos.gob.ec", "0987654321", CLAVE, Rol.TECNICO_MSP);
    }
}
