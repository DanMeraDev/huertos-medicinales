package com.huertos.config;

import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica la petición con el token Bearer. Sin token válido sigue como anónima.
 * El token deja de servir si la cuenta se desactiva o si la contraseña cambia. Mientras el
 * usuario deba cambiar su contraseña, solo recibe la autoridad que permite cambiarla.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String CAMBIO_CLAVE_PENDIENTE = "CAMBIO_CLAVE_PENDIENTE";
    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            jwtService.validar(encabezado.substring(PREFIJO_BEARER.length())).ifPresent(this::autenticar);
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(Jwt jwt) {
        usuarioRepository.findByCorreo(jwt.getSubject())
                .filter(Usuario::isActivo)
                .filter(usuario -> jwtService.huellaDe(usuario.getPasswordHash())
                        .equals(jwt.getClaimAsString(JwtService.CLAIM_HUELLA)))
                .ifPresent(usuario -> SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(usuario.getCorreo(), null, List.of(autoridadDe(usuario)))));
    }

    private SimpleGrantedAuthority autoridadDe(Usuario usuario) {
        if (usuario.isRequiereCambioClave()) {
            return new SimpleGrantedAuthority(CAMBIO_CLAVE_PENDIENTE);
        }
        return new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
    }
}
