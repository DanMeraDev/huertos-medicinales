package com.huertos.service.impl;

import com.huertos.dto.ActividadAncestralResponse;
import com.huertos.dto.ParticipanteActividadDto;
import com.huertos.dto.RegistrarActividadAncestralRequest;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.ActividadAncestralMapper;
import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.ActividadMspParticipante;
import com.huertos.model.Comunidad;
import com.huertos.model.Huerto;
import com.huertos.model.Miembro;
import com.huertos.model.SaberAncestral;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.ComunidadMiembroRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.SaberAncestralRepository;
import com.huertos.service.ActividadAncestralService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActividadAncestralServiceImpl implements ActividadAncestralService {

    private final ActividadAncestralMspRepository actividadAncestralRepository;
    private final SaberAncestralRepository saberAncestralRepository;
    private final ComunidadRepository comunidadRepository;
    private final HuertoRepository huertoRepository;
    private final MiembroRepository miembroRepository;
    private final ComunidadMiembroRepository comunidadMiembroRepository;
    private final ActividadAncestralMapper actividadAncestralMapper;

    @Override
    @Transactional
    public ActividadAncestralResponse registrarActividadGrande(String correo, RegistrarActividadAncestralRequest request) {
        Comunidad comunidad = comunidadRepository.findByResponsableCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ninguna comunidad asociada al responsable autenticado"));

        Huerto huerto = huertoRepository.findByComunidadId(comunidad.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad no tiene un huerto activo registrado"));

        SaberAncestral saber = saberAncestralRepository.findById(request.saberAncestralId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El saber ancestral seleccionado no existe"));

        ActividadAncestralMsp actividad = new ActividadAncestralMsp();
        actividad.setComunidad(comunidad);
        actividad.setHuerto(huerto);
        actividad.setSaberAncestral(saber);
        actividad.setFechaRealizacion(request.fechaRealizacion());
        actividad.setFaseLunarEjecucion(request.faseLunarEjecucion());
        actividad.setDescripcionBitacora(request.descripcionBitacora());
        actividad.setEvidenciaFotoUrl(request.evidenciaFotoUrl());
        actividad.setEvidenciaVideoUrl(request.evidenciaVideoUrl());

        Set<Long> miembroIdsRegistrados = new HashSet<>();
        for (ParticipanteActividadDto participante : request.participantes()) {
            if (!miembroIdsRegistrados.add(participante.miembroId())) {
                throw new ReglaNegocioException("El participante ya está registrado en la actividad");
            }

            if (!comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(
                    comunidad.getId(), participante.miembroId())) {
                throw new ReglaNegocioException("El participante seleccionado no es un miembro activo de la comunidad");
            }

            Miembro miembro = miembroRepository.findById(participante.miembroId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("El participante seleccionado no existe"));

            ActividadMspParticipante registro = new ActividadMspParticipante();
            registro.setActividadMsp(actividad);
            registro.setMiembro(miembro);
            registro.setHorasTrabajadas(participante.horasTrabajadas());
            registro.setPuntosAcreditados(0);
            actividad.getParticipantes().add(registro);
        }

        ActividadAncestralMsp guardada = actividadAncestralRepository.save(actividad);
        return actividadAncestralMapper.toActividadAncestralResponse(guardada);
    }
}
