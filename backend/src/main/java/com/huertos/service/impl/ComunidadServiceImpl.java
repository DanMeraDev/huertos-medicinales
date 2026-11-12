package com.huertos.service.impl;

import com.huertos.dto.ActualizarComunidadRequest;
import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.dto.ComunidadResponse;
import com.huertos.dto.DirectorioComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.ComunidadMapper;
import com.huertos.model.Comunidad;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadMiembroRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.ComunidadService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComunidadServiceImpl implements ComunidadService {

    private final ComunidadRepository comunidadRepository;
    private final HuertoRepository huertoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComunidadMiembroRepository comunidadMiembroRepository;
    private final ComunidadMapper comunidadMapper;
    private final Clock clock;

    @Override
    @Transactional
    public ComunidadResponse registrarComunidadDesdeSolicitud(SolicitudComunidad solicitud, Usuario responsable) {
        if (!responsable.isActivo()) {
            throw new ReglaNegocioException("El usuario asignado como responsable no es válido para liderar la comunidad");
        }
        if (comunidadRepository.existsByNombre(solicitud.getNombreComunidad())) {
            throw new ConflictoException("Ya existe una comunidad activa registrada con este nombre");
        }
        Comunidad comunidad = comunidadMapper.toComunidad(solicitud, responsable);
        comunidad.setCreadoEn(LocalDateTime.now(clock));
        Comunidad guardada = comunidadRepository.save(comunidad);
        huertoRepository.save(comunidadMapper.toHuerto(solicitud, guardada));
        return comunidadMapper.toComunidadResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ComunidadDetalleResponse obtenerPorResponsableId(Long responsableId) {
        Comunidad comunidad = comunidadRepository.findByResponsableId(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una comunidad asignada al usuario"));
        return comunidadMapper.toDetalleResponse(comunidad);
    }

    @Override
    @Transactional
    public ComunidadDetalleResponse actualizarInformacionContacto(Long responsableId, ActualizarComunidadRequest request) {
        Comunidad comunidad = comunidadRepository.findByResponsableId(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una comunidad asignada al usuario"));

        if (comunidadRepository.existsByNombreAndIdNot(request.nombre(), comunidad.getId())) {
            throw new ConflictoException("Ya existe una comunidad registrada con este nombre");
        }

        comunidad.setNombre(request.nombre());
        comunidad.setTelefonoContacto(request.telefonoContacto());
        comunidad.setCorreoContacto(request.correoContacto());

        Comunidad guardada = comunidadRepository.save(comunidad);
        return comunidadMapper.toDetalleResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ComunidadDetalleResponse obtenerPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
        return obtenerPorResponsableId(usuario.getId());
    }

    @Override
    @Transactional
    public ComunidadDetalleResponse actualizarPorCorreo(String correo, ActualizarComunidadRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
        return actualizarInformacionContacto(usuario.getId(), request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectorioComunidadResponse> obtenerDirectorioPublico() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}
