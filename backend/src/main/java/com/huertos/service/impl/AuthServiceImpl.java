package com.huertos.service.impl;

import com.huertos.config.JwtService;
import com.huertos.dto.LoginRequest;
import com.huertos.dto.LoginResponse;
import com.huertos.exception.CredencialesInvalidasException;
import com.huertos.exception.CuentaInactivaException;
import com.huertos.mapper.UsuarioMapper;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .filter(encontrado -> passwordEncoder.matches(request.password(), encontrado.getPasswordHash()))
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));
        if (!usuario.isActivo()) {
            throw new CuentaInactivaException(
                    "Esta cuenta se encuentra inactiva. Comuníquese con la administración del sistema");
        }
        String token = jwtService.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol().name(),
                usuario.isRequiereCambioClave());
        return usuarioMapper.toLoginResponse(usuario, token);
    }
}
