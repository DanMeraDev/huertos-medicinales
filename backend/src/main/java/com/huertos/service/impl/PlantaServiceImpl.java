package com.huertos.service.impl;

import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.mapper.PlantaMapper;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.PlantaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlantaServiceImpl implements PlantaService {

    private static final int TAMANIO_MINIMO = 1;
    private static final int TAMANIO_MAXIMO = 100;
    private static final Sort POR_NOMBRE_COMUN = Sort.by(Sort.Direction.ASC, "nombreComun");

    private final PlantaRepository plantaRepository;
    private final PlantaMapper plantaMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<PlantaResumenResponse> listarPlantasPublicas(int pagina, int tamanio) {
        PageRequest paginacion = PageRequest.of(
                Math.max(pagina, 0), Math.clamp(tamanio, TAMANIO_MINIMO, TAMANIO_MAXIMO), POR_NOMBRE_COMUN);
        return PaginaResponse.de(plantaRepository.findAllByActivaTrue(paginacion).map(plantaMapper::aResumen));
    }
}
