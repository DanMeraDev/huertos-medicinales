package com.huertos.config;

import com.huertos.repository.UsuarioRepository;
import jakarta.servlet.DispatcherType;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * API sin estado. Son públicas las rutas de acceso, diagnóstico y documentación;
 * el resto exige un token JWT válido y el rol que corresponde a cada ruta, que se revisa
 * antes de validar el cuerpo de la petición.
 */
@Configuration
@EnableMethodSecurity
@Import(ClockConfig.class)
public class SecurityConfig {

    private static final String TECNICO_SIS = "TECNICO_SIS";
    private static final String TECNICO_MSP = "TECNICO_MSP";
    private static final String RESPONSABLE_COMUNIDAD = "RESPONSABLE_COMUNIDAD";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
            UsuarioRepository usuarioRepository) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/publico/**", "/api/ping", "/actuator/health/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/cambiar-clave").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole(TECNICO_SIS)
                        .requestMatchers("/api/msp/**", "/api/solicitudes-comunidad/**").hasRole(TECNICO_MSP)
                        .requestMatchers(HttpMethod.GET, "/api/bodega/comunidades/*/material-vegetal")
                        .hasAnyRole(RESPONSABLE_COMUNIDAD, TECNICO_MSP)
                        .requestMatchers("/api/comunidad/**", "/api/comunidades/mi-comunidad/**",
                                "/api/actividades-ancestrales/**", "/api/bodega/**").hasRole(RESPONSABLE_COMUNIDAD)
                        .anyRequest().authenticated())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, usuarioRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    JwtService jwtService(
            @Value("${app.jwt.secreto}") String secreto,
            @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos,
            Clock clock) {
        return new JwtService(secreto, expiracionMinutos, clock);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
