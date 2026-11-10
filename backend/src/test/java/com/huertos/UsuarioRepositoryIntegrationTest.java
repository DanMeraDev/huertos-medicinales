package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class UsuarioRepositoryIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void guardarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Rosa Quishpe");
        usuario.setIdentificacion("1712345678");
        usuario.setGenero(Genero.FEMENINO);
        usuario.setEmail("rosa@correo.com");
        usuario.setTelefono("0991234567");
        usuario.setContrasena("hash");
        usuario.setRol(Rol.RESPONSABLE_COMUNIDAD);
        usuario.setRequiereCambioClave(true);
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.of(2026, 11, 5, 10, 0));
        usuarioRepository.saveAndFlush(usuario);
    }

    @Test
    void encuentraElUsuarioPorEmailConSusEnums() {
        Usuario encontrado = usuarioRepository.findByEmail("rosa@correo.com").orElseThrow();

        assertThat(encontrado.getRol()).isEqualTo(Rol.RESPONSABLE_COMUNIDAD);
        assertThat(encontrado.getGenero()).isEqualTo(Genero.FEMENINO);
        assertThat(encontrado.getTelefono()).isEqualTo("0991234567");
        assertThat(encontrado.isRequiereCambioClave()).isTrue();
    }

    @Test
    void detectaEmailEIdentificacionExistentes() {
        assertThat(usuarioRepository.existsByEmail("rosa@correo.com")).isTrue();
        assertThat(usuarioRepository.existsByIdentificacion("1712345678")).isTrue();
        assertThat(usuarioRepository.existsByEmail("otra@correo.com")).isFalse();
        assertThat(usuarioRepository.existsByIdentificacion("0000000000")).isFalse();
    }

    @Test
    void detectaSiExisteAlgunUsuarioConUnRol() {
        assertThat(usuarioRepository.existsByRol(Rol.RESPONSABLE_COMUNIDAD)).isTrue();
        assertThat(usuarioRepository.existsByRol(Rol.TECNICO_SIS)).isFalse();
    }
}
