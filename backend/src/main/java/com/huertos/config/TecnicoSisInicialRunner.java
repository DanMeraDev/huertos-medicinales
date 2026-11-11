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
 * Crea el primer TECNICO_SIS al arrancar, porque solo él puede crear las cuentas de los demás.
 * Las credenciales llegan por variables de entorno para no guardarlas en una migración.
 */
@Component
public class TecnicoSisInicialRunner implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String correo;
    private final String contrasena;
    private final String fechaNacimiento;

    public TecnicoSisInicialRunner(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            Clock clock,
            @Value("${app.admin.correo}") String correo,
            @Value("${app.admin.contrasena}") String contrasena,
            @Value("${app.admin.fecha-nacimiento}") String fechaNacimiento) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.correo = correo;
        this.contrasena = contrasena;
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (StringUtils.hasText(correo) && StringUtils.hasText(contrasena) && StringUtils.hasText(fechaNacimiento)
                && !usuarioRepository.existsByRol(Rol.TECNICO_SIS)) {
            usuarioRepository.save(crearTecnicoDeSistemas());
        }
    }

    private Usuario crearTecnicoDeSistemas() {
        Usuario tecnico = new Usuario();
        tecnico.setNombreCompleto("Técnico de sistemas");
        tecnico.setIdentificacion("TECNICO-SIS");
        tecnico.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        tecnico.setGenero(Genero.OTRO);
        tecnico.setCorreo(correo);
        tecnico.setPasswordHash(passwordEncoder.encode(contrasena));
        tecnico.setRol(Rol.TECNICO_SIS);
        tecnico.setRequiereCambioClave(false);
        tecnico.setActivo(true);
        tecnico.setCreadoEn(LocalDateTime.now(clock));
        return tecnico;
    }
}
