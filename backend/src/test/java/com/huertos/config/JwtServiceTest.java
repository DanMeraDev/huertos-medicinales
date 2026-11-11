package com.huertos.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtServiceTest {

    private static final String SECRETO = "clave-de-prueba-con-mas-de-32-caracteres";
    private static final Instant AHORA = Instant.parse("2026-11-05T10:00:00Z");

    private final JwtService jwtService = servicioEn(AHORA);

    @Test
    void generaUnTokenConElEmailYElRol() {
        String token = jwtService.generarToken("rosa@correo.com", "MIEMBRO");

        Jwt jwt = jwtService.validar(token).orElseThrow();

        assertThat(jwt.getSubject()).isEqualTo("rosa@correo.com");
        assertThat(jwt.getClaimAsString("rol")).isEqualTo("MIEMBRO");
        assertThat(jwt.getExpiresAt()).isEqualTo(AHORA.plus(Duration.ofMinutes(480)));
    }

    @Test
    void generaUnTokenConIdYRequiereCambioClave() {
        String token = jwtService.generarToken(15L, "rosa@correo.com", "RESPONSABLE_COMUNIDAD", true);

        Jwt jwt = jwtService.validar(token).orElseThrow();

        assertThat(jwt.getSubject()).isEqualTo("rosa@correo.com");
        assertThat(jwt.<Long>getClaim("id")).isEqualTo(15L);
        assertThat(jwt.getClaimAsString("rol")).isEqualTo("RESPONSABLE_COMUNIDAD");
        assertThat(jwt.<Boolean>getClaim("requiereCambioClave")).isTrue();
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        JwtService otroServicio = new JwtService(
                "otra-clave-de-prueba-con-mas-de-32-caracteres", 480, relojEn(AHORA));

        String token = otroServicio.generarToken("rosa@correo.com", "ADMIN_MSP");

        assertThat(jwtService.validar(token)).isEmpty();
    }

    @Test
    void rechazaUnTokenExpirado() {
        String token = jwtService.generarToken("rosa@correo.com", "MIEMBRO");

        JwtService despuesDeExpirar = servicioEn(AHORA.plus(Duration.ofMinutes(481)));

        assertThat(despuesDeExpirar.validar(token)).isEmpty();
    }

    @Test
    void rechazaUnTextoQueNoEsToken() {
        assertThat(jwtService.validar("esto-no-es-un-token")).isEmpty();
    }

    private static JwtService servicioEn(Instant instante) {
        return new JwtService(SECRETO, 480, relojEn(instante));
    }

    private static Clock relojEn(Instant instante) {
        return Clock.fixed(instante, ZoneOffset.UTC);
    }
}
