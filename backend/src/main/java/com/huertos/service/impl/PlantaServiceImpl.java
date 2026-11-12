package com.huertos.service.impl;

import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.mapper.PlantaMapper;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.PlantaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlantaServiceImpl implements PlantaService {

    private final PlantaRepository plantaRepository;
    private final PlantaMapper plantaMapper;

    @Override
    public PaginaResponse<PlantaResumenResponse> listarPlantasPublicas(int pagina, int tamanio) {
        throw new UnsupportedOperationException();
    }
}
