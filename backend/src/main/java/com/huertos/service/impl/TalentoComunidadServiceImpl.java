package com.huertos.service.impl;

import com.huertos.dto.CrearTalentoComunidadRequest;
import com.huertos.dto.TalentoComunidadResponse;
import com.huertos.mapper.TalentoComunidadMapper;
import com.huertos.repository.ActividadComunidadRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.TalentoComunidadRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.TalentoComunidadService;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TalentoComunidadServiceImpl implements TalentoComunidadService {

    private final TalentoComunidadRepository talentoComunidadRepository;
    private final ComunidadRepository comunidadRepository;
    private final ActividadComunidadRepository actividadComunidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final TalentoComunidadMapper talentoComunidadMapper;
    private final Clock clock;

    @Override
    public List<TalentoComunidadResponse> listarPorResponsable(Long responsableId) {
        return null;
    }

    @Override
    public List<TalentoComunidadResponse> listarPorCorreo(String correo) {
        return null;
    }

    @Override
    public TalentoComunidadResponse registrarTalento(Long responsableId, CrearTalentoComunidadRequest request) {
        return null;
    }

    @Override
    public TalentoComunidadResponse registrarTalentoPorCorreo(String correo, CrearTalentoComunidadRequest request) {
        return null;
    }
}
