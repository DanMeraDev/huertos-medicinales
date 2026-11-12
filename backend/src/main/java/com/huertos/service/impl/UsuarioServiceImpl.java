package com.huertos.service.impl;

import com.huertos.dto.CambiarClaveRequest;
import com.huertos.dto.CambioClaveResponse;
import com.huertos.dto.CrearUsuarioRequest;
import com.huertos.dto.UsuarioDetalleResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.UsuarioMapper;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.UsuarioService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private static final int EDAD_MINIMA = 18;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final UsuarioMapper usuarioMapper;

    @Override
    public UsuarioDetalleResponse crearUsuario(CrearUsuarioRequest request) {
        return usuarioMapper.toUsuarioDetalleResponse(crearUsuarioInterno(request));
    }

    @Override
    public Usuario crearUsuarioInterno(CrearUsuarioRequest request) {
        validarMayoriaDeEdad(request.fechaNacimiento());
        validarDatosUnicos(request);
        Usuario usuario = usuarioMapper.toUsuario(request, passwordEncoder.encode(request.password()));
        usuario.setCreadoEn(LocalDateTime.now(clock));
        return usuarioRepository.save(usuario);
    }

    @Override
    public CambioClaveResponse cambiarClave(String correo, CambiarClaveRequest request) {
        if (!request.nuevaPassword().equals(request.confirmarPassword())) {
            throw new ReglaNegocioException("La nueva contraseña y su confirmación no coinciden");
        }
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new ReglaNegocioException("La contraseña actual ingresada es incorrecta");
        }
        if (request.nuevaPassword().equals(request.passwordActual())) {
            throw new ConflictoException("La nueva contraseña no puede ser idéntica a la contraseña actual");
        }
        usuario.setPasswordHash(passwordEncoder.encode(request.nuevaPassword()));
        usuario.setRequiereCambioClave(false);
        usuarioRepository.save(usuario);
        return new CambioClaveResponse("Contraseña actualizada exitosamente", false);
    }

    private void validarMayoriaDeEdad(LocalDate fechaNacimiento) {
        if (Period.between(fechaNacimiento, LocalDate.now(clock)).getYears() < EDAD_MINIMA) {
            throw new ReglaNegocioException("El usuario debe ser mayor de 18 años para ser registrado en la plataforma");
        }
    }

    private void validarDatosUnicos(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByIdentificacion(request.identificacion())) {
            throw new ConflictoException("Ya existe un usuario registrado con la cédula ingresada");
        }
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new ConflictoException("Ya existe un usuario registrado con el correo electrónico ingresado");
        }
    }
}
