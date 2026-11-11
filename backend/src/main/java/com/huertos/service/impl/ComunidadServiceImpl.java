package com.huertos.service.impl;

import com.huertos.dto.ComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.ComunidadMapper;
import com.huertos.model.Comunidad;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.Usuario;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.service.ComunidadService;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComunidadServiceImpl implements ComunidadService {

    private final ComunidadRepository comunidadRepository;
    private final HuertoRepository huertoRepository;
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
}
