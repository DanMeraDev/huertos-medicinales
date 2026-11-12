package com.huertos.service.impl;

import com.huertos.dto.ActualizarComunidadRequest;
import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.mapper.ComunidadMapper;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.ComunidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComunidadServiceImpl implements ComunidadService {

    private final ComunidadRepository comunidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComunidadMapper comunidadMapper;

    @Override
    @Transactional(readOnly = true)
    public ComunidadDetalleResponse obtenerPorResponsableId(Long responsableId) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional
    public ComunidadDetalleResponse actualizarInformacionContacto(Long responsableId, ActualizarComunidadRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional(readOnly = true)
    public ComunidadDetalleResponse obtenerPorCorreo(String correo) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional
    public ComunidadDetalleResponse actualizarPorCorreo(String correo, ActualizarComunidadRequest request) {
        throw new UnsupportedOperationException();
    }
}
