package com.huertos.service.impl;

import com.huertos.dto.ActualizarRolRequest;
import com.huertos.dto.UsuarioResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.UsuarioMapper;
import com.huertos.model.AuditoriaRol;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.AuditoriaRolRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.AdminUsuarioService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUsuarioServiceImpl implements AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRolRepository auditoriaRolRepository;
    private final UsuarioMapper usuarioMapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::aResponse)
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarRol(Long usuarioId, ActualizarRolRequest request, String emailAdministrador) {
        Usuario usuario = buscarUsuario(usuarioId);
        validarQueQuedeUnAdministrador(usuario, request.nuevoRol());
        registrarAuditoria(usuario, request.nuevoRol(), emailAdministrador);
        usuario.setRol(request.nuevoRol());
        return usuarioMapper.aResponse(usuario);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el ID especificado."));
    }

    private void validarQueQuedeUnAdministrador(Usuario usuario, Rol nuevoRol) {
        boolean pierdeElRol = usuario.getRol() == Rol.ADMIN_MSP && nuevoRol != Rol.ADMIN_MSP;
        if (pierdeElRol && usuario.isActivo() && usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN_MSP) <= 1) {
            throw new ReglaNegocioException("No es posible cambiar el rol del único administrador activo del sistema.");
        }
    }

    private void registrarAuditoria(Usuario usuario, Rol nuevoRol, String emailAdministrador) {
        Usuario administrador = usuarioRepository.findByEmail(emailAdministrador)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el administrador de la sesión."));
        AuditoriaRol auditoria = new AuditoriaRol();
        auditoria.setUsuarioId(usuario.getId());
        auditoria.setRolAnterior(usuario.getRol());
        auditoria.setRolNuevo(nuevoRol);
        auditoria.setModificadoPor(administrador.getId());
        auditoria.setFechaCambio(LocalDateTime.now(clock));
        auditoriaRolRepository.save(auditoria);
    }
}
