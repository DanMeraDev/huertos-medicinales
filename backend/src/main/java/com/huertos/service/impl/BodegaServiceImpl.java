package com.huertos.service.impl;

import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.BodegaMapper;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.Comunidad;
import com.huertos.model.Planta;
import com.huertos.repository.BodegaMaterialVegetalRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.BodegaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BodegaServiceImpl implements BodegaService {

    private final BodegaMaterialVegetalRepository bodegaRepository;
    private final ComunidadRepository comunidadRepository;
    private final PlantaRepository plantaRepository;
    private final BodegaMapper bodegaMapper;

    @Override
    @Transactional
    public MaterialVegetalResponse registrarIngresoMaterial(Long comunidadId, RegistrarMaterialVegetalRequest request) {
        if (comunidadId == null) {
            throw new RecursoNoEncontradoException("No se especificó la comunidad para el ingreso de material");
        }
        Comunidad comunidad = comunidadRepository.findById(comunidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La comunidad indicada no existe"));

        Planta planta = plantaRepository.findByIdAndActivaTrue(request.plantaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La planta seleccionada no existe en el catálogo"));

        BodegaMaterialVegetal material = new BodegaMaterialVegetal();
        material.setComunidad(comunidad);
        material.setPlanta(planta);
        material.setTipoMaterial(request.tipoMaterial());
        material.setCantidadDisponible(request.cantidad());
        material.setUnidadMedida(request.unidadMedida());
        material.setLoteOrigen(request.loteOrigen());
        material.setFechaIngreso(request.fechaIngreso());
        material.setActivo(true);

        BodegaMaterialVegetal guardado = bodegaRepository.save(material);
        return bodegaMapper.toMaterialVegetalResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerComunidadIdPorCorreo(String correo) {
        return comunidadRepository.findByResponsableCorreo(correo)
                .map(Comunidad::getId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una comunidad asociada al usuario"));
    }
}
