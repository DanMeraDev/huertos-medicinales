package com.huertos.config;

import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Crea el primer ADMIN_MSP al arrancar, porque ningún rol puede asignarse sin un administrador.
 * Las credenciales llegan por variables de entorno para no guardarlas en una migración.
 */
@Component
public class AdminInicialRunner implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String email;
    private final String contrasena;

    public AdminInicialRunner(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            Clock clock,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.contrasena}") String contrasena) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.email = email;
        this.contrasena = contrasena;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (StringUtils.hasText(email) && StringUtils.hasText(contrasena)
                && !usuarioRepository.existsByRol(Rol.ADMIN_MSP)) {
            usuarioRepository.save(crearAdministrador());
        }
    }

    private Usuario crearAdministrador() {
        Usuario administrador = new Usuario();
        administrador.setNombreCompleto("Administrador MSP");
        administrador.setIdentificacion("ADMIN-MSP");
        administrador.setFechaNacimiento(LocalDate.EPOCH);
        administrador.setGenero(Genero.OTRO);
        administrador.setEmail(email);
        administrador.setContrasena(passwordEncoder.encode(contrasena));
        administrador.setRol(Rol.ADMIN_MSP);
        administrador.setActivo(true);
        administrador.setCreadoEn(LocalDateTime.now(clock));
        return administrador;
    }
}
