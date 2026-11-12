package com.huertos.service.impl;

import com.huertos.dto.CrearTalentoComunidadRequest;
import com.huertos.dto.TalentoComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.TalentoComunidadMapper;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.Comunidad;
import com.huertos.model.TalentoComunidad;
import com.huertos.model.Usuario;
import com.huertos.repository.ActividadComunidadRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.TalentoComunidadRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.TalentoComunidadService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public List<TalentoComunidadResponse> listarPorResponsable(Long responsableId) {
        Comunidad comunidad = comunidadRepository.findByResponsableId(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ninguna comunidad asociada al responsable autenticado"));

        return talentoComunidadRepository.findByComunidadIdAndActivoTrue(comunidad.getId()).stream()
                .map(talentoComunidadMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TalentoComunidadResponse> listarPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
        return listarPorResponsable(usuario.getId());
    }

    @Override
    @Transactional
    public TalentoComunidadResponse registrarTalento(Long responsableId, CrearTalentoComunidadRequest request) {
        Comunidad comunidad = comunidadRepository.findByResponsableId(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ninguna comunidad asociada al responsable autenticado"));

        ActividadComunidad actividad = actividadComunidadRepository.findById(request.actividadComunidadId())
                .filter(a -> a.getComunidad().getId().equals(comunidad.getId()))
                .orElseThrow(() -> new RecursoNoEncontradoException("La actividad comunitaria especificada no existe o no pertenece a tu comunidad"));

        if (talentoComunidadRepository.existsByComunidadIdAndNombreInsigniaAndActivoTrue(comunidad.getId(), request.nombreInsignia())) {
            throw new ConflictoException("Ya existe un talento o medalla registrada con este nombre en tu comunidad");
        }

        TalentoComunidad talento = talentoComunidadMapper.toEntity(request, comunidad, actividad);
        talento.setIconoCodigo("insignia-semilla");
        talento.setActivo(true);
        talento.setCreadoEn(LocalDateTime.now(clock));

        TalentoComunidad guardado = talentoComunidadRepository.save(talento);
        return talentoComunidadMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public TalentoComunidadResponse registrarTalentoPorCorreo(String correo, CrearTalentoComunidadRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
        return registrarTalento(usuario.getId(), request);
    }
}
