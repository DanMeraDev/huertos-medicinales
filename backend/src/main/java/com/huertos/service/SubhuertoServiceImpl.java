package com.huertos.service;

import com.huertos.dto.CrearSubhuertoRequest;
import com.huertos.dto.SubhuertoResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.Comunidad;
import com.huertos.model.Huerto;
import com.huertos.model.Subhuerto;
import com.huertos.model.SubtipoSubhuerto;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.SubhuertoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubhuertoServiceImpl implements SubhuertoService {

    private static final int MAX_SUBTIPOS_MSP = 3;

    private final SubhuertoRepository subhuertoRepository;
    private final HuertoRepository huertoRepository;
    private final ComunidadRepository comunidadRepository;

    @Override
    @Transactional
    public SubhuertoResponse crear(String correoResponsable, CrearSubhuertoRequest request) {
        Huerto huerto = obtenerHuertoComunidad(correoResponsable);

        if (huerto.getAreaTotalM2() == null) {
            throw new ReglaNegocioException("Primero completa el área del huerto");
        }

        List<Subhuerto> activos = subhuertoRepository.findByHuertoIdAndActivoTrueOrderByCreadoEnAsc(huerto.getId());

        Set<SubtipoSubhuerto> subtiposEnUso = activos.stream()
                .map(Subhuerto::getSubtipo)
                .collect(Collectors.toSet());

        if (!subtiposEnUso.contains(request.subtipo()) && subtiposEnUso.size() >= MAX_SUBTIPOS_MSP) {
            throw new ReglaNegocioException("Máximo 3 subtipos por huerto según norma MSP");
        }

        BigDecimal areaOcupada = activos.stream()
                .map(Subhuerto::getAreaM2)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (areaOcupada.add(request.areaM2()).compareTo(huerto.getAreaTotalM2()) > 0) {
            throw new ReglaNegocioException("El área ingresada supera el espacio disponible del huerto");
        }

        Subhuerto nuevo = new Subhuerto();
        nuevo.setHuerto(huerto);
        nuevo.setNombreZona(request.nombreZona().trim());
        nuevo.setSubtipo(request.subtipo());
        nuevo.setAreaM2(request.areaM2());
        nuevo.setActivo(true);

        Subhuerto guardado = subhuertoRepository.save(nuevo);
        return aResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubhuertoResponse> listarPorUsuario(String correoUsuario) {
        Huerto huerto = obtenerHuertoComunidad(correoUsuario);
        return subhuertoRepository.findByHuertoIdAndActivoTrueOrderByCreadoEnAsc(huerto.getId())
                .stream()
                .map(this::aResponse)
                .toList();
    }

    private Huerto obtenerHuertoComunidad(String correoResponsable) {
        Comunidad comunidad = comunidadRepository.findByResponsableCorreo(correoResponsable)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una comunidad asociada al usuario"));

        return huertoRepository.findByComunidadId(comunidad.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el huerto de la comunidad"));
    }

    private SubhuertoResponse aResponse(Subhuerto subhuerto) {
        return new SubhuertoResponse(
                subhuerto.getId(),
                subhuerto.getHuerto().getId(),
                subhuerto.getNombreZona(),
                subhuerto.getSubtipo(),
                subhuerto.getAreaM2(),
                subhuerto.isActivo()
        );
    }
}