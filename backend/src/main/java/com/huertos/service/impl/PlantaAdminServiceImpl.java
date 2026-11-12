package com.huertos.service.impl;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.mapper.PlantaMapper;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.PlantaAdminService;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlantaAdminServiceImpl implements PlantaAdminService {

    private final PlantaRepository plantaRepository;
    private final PlantaMapper plantaMapper;
    private final Clock clock;

    @Override
    public PlantaDetalleResponse registrarPlanta(CrearPlantaRequest request) {
        return null;
    }
}
