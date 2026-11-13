package com.huertos.service.impl;

import com.huertos.dto.RegistrarSiembraRequest;
import com.huertos.dto.SiembraDetalleResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.SiembraMapper;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.Comunidad;
import com.huertos.model.EstadoCultivo;
import com.huertos.model.Huerto;
import com.huertos.model.Planta;
import com.huertos.model.Siembra;
import com.huertos.model.Subhuerto;
import com.huertos.repository.BodegaMaterialVegetalRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.SiembraRepository;
import com.huertos.repository.SubhuertoRepository;
import com.huertos.service.BodegaService;
import com.huertos.service.SiembraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiembraServiceImpl implements SiembraService {

    private final SiembraRepository siembraRepository;
    private final SubhuertoRepository subhuertoRepository;
    private final BodegaMaterialVegetalRepository bodegaMaterialVegetalRepository;
    private final HuertoRepository huertoRepository;
    private final ComunidadRepository comunidadRepository;
    private final BodegaService bodegaService;
    private final SiembraMapper siembraMapper;

    @Override
    @Transactional
    public SiembraDetalleResponse registrarSiembra(String correoResponsable, RegistrarSiembraRequest request) {
        Comunidad comunidad = comunidadRepository.findByResponsableCorreo(correoResponsable)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una comunidad asociada al usuario"));

        Huerto huerto = huertoRepository.findByComunidadId(comunidad.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el huerto de la comunidad"));

        Subhuerto subhuerto = subhuertoRepository.findById(request.subhuertoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El subhuerto especificado no existe"));

        if (!subhuerto.isActivo() || !subhuerto.getHuerto().getId().equals(huerto.getId())) {
            throw new ReglaNegocioException("El subhuerto no está activo o no pertenece a su huerto");
        }

        BodegaMaterialVegetal material = bodegaMaterialVegetalRepository.findById(request.bodegaMaterialId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El material vegetal especificado no existe en bodega"));

        if (!material.isActivo() || !material.getComunidad().getId().equals(comunidad.getId())) {
            throw new ReglaNegocioException("El material vegetal no pertenece a la bodega de su comunidad");
        }

        if (material.getCantidadDisponible().compareTo(request.cantidadSembrada()) < 0) {
            throw new ReglaNegocioException("Stock insuficiente en bodega");
        }

        Planta planta = material.getPlanta();
        int altitudHuerto = huerto.getAltitudMsnm();
        boolean fueraDeRango = (planta.getAltitudMinMsnm() != null && altitudHuerto < planta.getAltitudMinMsnm())
                || (planta.getAltitudMaxMsnm() != null && altitudHuerto > planta.getAltitudMaxMsnm());

        if (fueraDeRango && !Boolean.TRUE.equals(request.aceptaRiesgoAltitud())) {
            throw new ReglaNegocioException("Discrepancia de altitud: la altitud del huerto (" + altitudHuerto
                    + " msnm) se encuentra fuera del rango óptimo para " + planta.getNombreComun()
                    + " (" + planta.getAltitudMinMsnm() + " - " + planta.getAltitudMaxMsnm() + " msnm)");
        }

        bodegaService.descontarStockPorSiembra(material.getId(), request.cantidadSembrada());

        Siembra siembra = new Siembra();
        siembra.setSubhuerto(subhuerto);
        siembra.setBodegaMaterial(material);
        siembra.setCantidadSembrada(request.cantidadSembrada());
        siembra.setFechaSiembra(request.fechaSiembra());
        siembra.setFaseLunarSiembra(request.faseLunarSiembra());
        siembra.setTuvoAlertaAltitud(fueraDeRango);
        siembra.setEstadoCultivo(EstadoCultivo.SEMBRADO);

        Siembra guardada = siembraRepository.save(siembra);
        return siembraMapper.toSiembraDetalleResponse(guardada);
    }
}

