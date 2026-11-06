package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import java.time.LocalDate;
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
        usuario.setFechaNacimiento(LocalDate.of(1990, 5, 20));
        usuario.setGenero(Genero.FEMENINO);
        usuario.setEmail("rosa@correo.com");
        usuario.setContrasena("hash");
        usuario.setRol(Rol.MIEMBRO);
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.of(2026, 11, 5, 10, 0));
        usuarioRepository.saveAndFlush(usuario);
    }

    @Test
    void encuentraElUsuarioPorEmailConSusEnums() {
        Usuario encontrado = usuarioRepository.findByEmail("rosa@correo.com").orElseThrow();

        assertThat(encontrado.getRol()).isEqualTo(Rol.MIEMBRO);
        assertThat(encontrado.getGenero()).isEqualTo(Genero.FEMENINO);
    }

    @Test
    void detectaEmailEIdentificacionExistentes() {
        assertThat(usuarioRepository.existsByEmail("rosa@correo.com")).isTrue();
        assertThat(usuarioRepository.existsByIdentificacion("1712345678")).isTrue();
        assertThat(usuarioRepository.existsByEmail("otra@correo.com")).isFalse();
        assertThat(usuarioRepository.existsByIdentificacion("0000000000")).isFalse();
    }
}
