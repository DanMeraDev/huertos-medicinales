package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.config.JwtService;
import com.huertos.model.AuditoriaRol;
import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.AuditoriaRolRepository;
import com.huertos.repository.UsuarioRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.admin.email=admin@correo.com", "app.admin.contrasena=clave-inicial"})
@Import(TestcontainersConfiguration.class)
class AdminUsuarioIntegrationTest {

    private static final String EMAIL_ADMIN = "admin@correo.com";

    @LocalServerPort
    private int puerto;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaRolRepository auditoriaRolRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void elArranqueCreaElAdministradorInicial() {
        Usuario administrador = usuarioRepository.findByEmail(EMAIL_ADMIN).orElseThrow();

        assertThat(administrador.getRol()).isEqualTo(Rol.ADMIN_MSP);
        assertThat(passwordEncoder.matches("clave-inicial", administrador.getContrasena())).isTrue();
    }

    @Test
    void cambiarElRolLoGuardaYDejaLaAuditoria() throws Exception {
        Usuario usuario = guardarMiembro();

        HttpResponse<String> respuesta = cambiarRol(usuario.getId(), "RESPONSABLE_COMUNIDAD");

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(usuarioRepository.findById(usuario.getId()).orElseThrow().getRol())
                .isEqualTo(Rol.RESPONSABLE_COMUNIDAD);
        AuditoriaRol auditoria = auditoriaRolRepository.findAll().stream()
                .filter(a -> a.getUsuarioId().equals(usuario.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(auditoria.getRolAnterior()).isEqualTo(Rol.MIEMBRO);
        assertThat(auditoria.getRolNuevo()).isEqualTo(Rol.RESPONSABLE_COMUNIDAD);
        assertThat(auditoria.getModificadoPor())
                .isEqualTo(usuarioRepository.findByEmail(EMAIL_ADMIN).orElseThrow().getId());
    }

    @Test
    void noSePuedeDegradarAlUnicoAdministrador() throws Exception {
        Long administradorId = usuarioRepository.findByEmail(EMAIL_ADMIN).orElseThrow().getId();

        HttpResponse<String> respuesta = cambiarRol(administradorId, "MIEMBRO");

        assertThat(respuesta.statusCode()).isEqualTo(400);
        assertThat(usuarioRepository.findById(administradorId).orElseThrow().getRol()).isEqualTo(Rol.ADMIN_MSP);
    }

    private Usuario guardarMiembro() {
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
        return usuarioRepository.save(usuario);
    }

    private HttpResponse<String> cambiarRol(Long id, String rol) throws Exception {
        HttpRequest peticion = HttpRequest
                .newBuilder(URI.create("http://localhost:" + puerto + "/api/admin/usuarios/" + id + "/rol"))
                .header("Authorization", "Bearer " + jwtService.generarToken(EMAIL_ADMIN, "ADMIN_MSP"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"nuevoRol\":\"" + rol + "\"}"))
                .build();
        try (HttpClient cliente = HttpClient.newHttpClient()) {
            return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
        }
    }
}
