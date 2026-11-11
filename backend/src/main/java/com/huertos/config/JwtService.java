package com.huertos.config;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Genera y valida los tokens de sesión, firmados con HS256.
 */
public class JwtService {

    private final JwtEncoder codificador;
    private final JwtDecoder decodificador;
    private final Duration expiracion;
    private final Clock clock;

    public JwtService(String secreto, long expiracionMinutos, Clock clock) {
        SecretKey clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtTimestampValidator validadorDeFechas = new JwtTimestampValidator(Duration.ZERO);
        validadorDeFechas.setClock(clock);
        NimbusJwtDecoder decodificadorNimbus = NimbusJwtDecoder.withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decodificadorNimbus.setJwtValidator(validadorDeFechas);

        this.codificador = NimbusJwtEncoder.withSecretKey(clave).build();
        this.decodificador = decodificadorNimbus;
        this.expiracion = Duration.ofMinutes(expiracionMinutos);
        this.clock = clock;
    }

    public String generarToken(String email, String rol) {
        return generarToken(null, email, rol, false);
    }

    public String generarToken(Long id, String email, String rol, boolean requiereCambioClave) {
        Instant ahora = clock.instant();
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("requiereCambioClave", requiereCambioClave)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expiracion));
        if (id != null) {
            builder.claim("id", id);
        }
        JwtClaimsSet datos = builder.build();
        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
        return codificador.encode(JwtEncoderParameters.from(encabezado, datos)).getTokenValue();
    }

    public Optional<Jwt> validar(String token) {
        try {
            return Optional.of(decodificador.decode(token));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}
