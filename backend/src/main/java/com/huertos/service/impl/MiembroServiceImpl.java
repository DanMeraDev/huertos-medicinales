package com.huertos.service.impl;

import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import com.huertos.mapper.MiembroMapper;
import com.huertos.repository.ComunidadMiembroRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.MiembroService;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MiembroServiceImpl implements MiembroService {

    private final MiembroRepository miembroRepository;
    private final ComunidadMiembroRepository comunidadMiembroRepository;
    private final ComunidadRepository comunidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final MiembroMapper miembroMapper;
    private final Clock clock;

    @Override
    public MiembroDetalleResponse registrarMiembroEnComunidad(Long responsableId, RegistrarMiembroRequest request) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public MiembroDetalleResponse registrarMiembroPorCorreo(String correo, RegistrarMiembroRequest request) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}
