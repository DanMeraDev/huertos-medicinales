package com.huertos.service.impl;

import com.huertos.config.JwtService;
import com.huertos.dto.LoginRequest;
import com.huertos.dto.LoginResponse;
import com.huertos.exception.CredencialesInvalidasException;
import com.huertos.exception.CuentaInactivaException;
import com.huertos.exception.DemasiadosIntentosException;
import com.huertos.mapper.UsuarioMapper;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.AuthService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int INTENTOS_MAXIMOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;
    private final Clock clock;
    private final Map<String, IntentosFallidos> intentosPorCorreo = new ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest request) {
        validarQueNoEsteBloqueado(request.correo());
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .filter(encontrado -> passwordEncoder.matches(request.password(), encontrado.getPasswordHash()))
                .orElseThrow(() -> {
                    registrarIntentoFallido(request.correo());
                    return new CredencialesInvalidasException("Correo o contraseña incorrectos");
                });
        intentosPorCorreo.remove(request.correo());
        if (!usuario.isActivo()) {
            throw new CuentaInactivaException(
                    "Esta cuenta se encuentra inactiva. Comuníquese con la administración del sistema");
        }
        return usuarioMapper.toLoginResponse(usuario, jwtService.generarToken(usuario));
    }

    private void validarQueNoEsteBloqueado(String correo) {
        IntentosFallidos intentos = intentosPorCorreo.get(correo);
        if (intentos != null && intentos.cantidad() >= INTENTOS_MAXIMOS && !bloqueoTerminado(intentos)) {
            throw new DemasiadosIntentosException("Demasiados intentos fallidos. Intente nuevamente en 15 minutos");
        }
    }

    private void registrarIntentoFallido(String correo) {
        Instant ahora = clock.instant();
        intentosPorCorreo.merge(correo, new IntentosFallidos(1, ahora), (anteriores, nuevo) ->
                bloqueoTerminado(anteriores) ? nuevo : new IntentosFallidos(anteriores.cantidad() + 1, ahora));
    }

    private boolean bloqueoTerminado(IntentosFallidos intentos) {
        return !clock.instant().isBefore(intentos.ultimoIntento().plus(DURACION_BLOQUEO));
    }

    private record IntentosFallidos(int cantidad, Instant ultimoIntento) {
    }
}
