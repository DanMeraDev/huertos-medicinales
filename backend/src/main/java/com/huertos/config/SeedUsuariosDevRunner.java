package com.huertos.config;

import com.huertos.model.Comunidad;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.Genero;
import com.huertos.model.Huerto;
import com.huertos.model.Rol;
import com.huertos.model.TipoHuerto;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedUsuariosDevRunner implements ApplicationRunner {

    private static final String CLAVE_PRUEBA = "Pruebas123!";
    private static final String CORREO_RESPONSABLE = "responsable.comunidad@huertos.gob.ec";
    private static final String NOMBRE_COMUNIDAD_PRUEBA = "Comunidad San Pedro de Pruebas";

    private static final List<CuentaSemilla> CUENTAS = List.of(
            new CuentaSemilla(
                    "Tecnico de Sistemas (Pruebas)",
                    "1700000001",
                    LocalDate.of(1990, 1, 15),
                    Genero.OTRO,
                    "tecnico.sis@huertos.gob.ec",
                    "0990000001",
                    Rol.TECNICO_SIS),
            new CuentaSemilla(
                    "Tecnico MSP (Pruebas)",
                    "1700000002",
                    LocalDate.of(1991, 5, 20),
                    Genero.FEMENINO,
                    "tecnico.msp@huertos.gob.ec",
                    "0990000002",
                    Rol.TECNICO_MSP),
            new CuentaSemilla(
                    "Responsable de Comunidad (Pruebas)",
                    "1700000003",
                    LocalDate.of(1992, 8, 10),
                    Genero.MASCULINO,
                    CORREO_RESPONSABLE,
                    "0990000003",
                    Rol.RESPONSABLE_COMUNIDAD));

    private final UsuarioRepository usuarioRepository;
    private final ComunidadRepository comunidadRepository;
    private final HuertoRepository huertoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (CuentaSemilla cuenta : CUENTAS) {
            if (!usuarioRepository.existsByCorreo(cuenta.correo())
                    && !usuarioRepository.existsByIdentificacion(cuenta.identificacion())) {
                usuarioRepository.save(construirUsuario(cuenta));
            }
        }
        asegurarComunidadYHuerto();
    }

    private Usuario construirUsuario(CuentaSemilla cuenta) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(cuenta.nombreCompleto());
        usuario.setIdentificacion(cuenta.identificacion());
        usuario.setFechaNacimiento(cuenta.fechaNacimiento());
        usuario.setGenero(cuenta.genero());
        usuario.setCorreo(cuenta.correo());
        usuario.setTelefono(cuenta.telefono());
        usuario.setPasswordHash(passwordEncoder.encode(CLAVE_PRUEBA));
        usuario.setRequiereCambioClave(false);
        usuario.setRol(cuenta.rol());
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.now(clock));
        return usuario;
    }

    private void asegurarComunidadYHuerto() {
        usuarioRepository.findByCorreo(CORREO_RESPONSABLE).ifPresent(responsable -> {
            if (comunidadRepository.findByResponsableId(responsable.getId()).isEmpty()
                    && !comunidadRepository.existsByNombre(NOMBRE_COMUNIDAD_PRUEBA)) {
                Comunidad comunidad = comunidadRepository.save(construirComunidad(responsable));
                huertoRepository.save(construirHuerto(comunidad));
            }
        });
    }

    private Comunidad construirComunidad(Usuario responsable) {
        Comunidad comunidad = new Comunidad();
        comunidad.setNombre(NOMBRE_COMUNIDAD_PRUEBA);
        comunidad.setTipoOrganizacion(TipoOrganizacion.BARRIO);
        comunidad.setProvincia("Pichincha");
        comunidad.setParroquia("Iñaquito");
        comunidad.setDireccion("Av. Amazonas N34-120");
        comunidad.setTelefonoContacto("0990000003");
        comunidad.setCorreoContacto(CORREO_RESPONSABLE);
        comunidad.setResponsable(responsable);
        comunidad.setPuntosComunidad(0);
        comunidad.setActiva(true);
        comunidad.setCreadoEn(LocalDateTime.now(clock));
        return comunidad;
    }

    private Huerto construirHuerto(Comunidad comunidad) {
        Huerto huerto = new Huerto();
        huerto.setComunidad(comunidad);
        huerto.setNombreHuerto("Huerto San Pedro de Pruebas");
        huerto.setLatitud(new BigDecimal("-0.1806530"));
        huerto.setLongitud(new BigDecimal("-78.4678340"));
        huerto.setAltitudMsnm(2850);
        huerto.setTipoHuerto(TipoHuerto.BASICO);
        huerto.setDisenoPrincipal(DisenoPrincipal.EN_TIERRA);
        huerto.setPuntosHuerto(0);
        huerto.setActivo(true);
        return huerto;
    }

    private record CuentaSemilla(
            String nombreCompleto,
            String identificacion,
            LocalDate fechaNacimiento,
            Genero genero,
            String correo,
            String telefono,
            Rol rol) {}
}

