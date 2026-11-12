package com.huertos.service.impl;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.PlantaMapper;
import com.huertos.model.Planta;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.PlantaAdminService;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlantaAdminServiceImpl implements PlantaAdminService {

    private final PlantaRepository plantaRepository;
    private final PlantaMapper plantaMapper;
    private final Clock clock;

    @Override
    @Transactional
    public PlantaDetalleResponse registrarPlanta(CrearPlantaRequest request) {
        validarRangoAltitud(request.altitudMinMsnm(), request.altitudMaxMsnm());
        CrearPlantaRequest normalizada = normalizar(request);
        if (plantaRepository.existsByNombreCientificoIgnoreCase(normalizada.nombreCientifico())) {
            throw new ConflictoException("El nombre científico ya existe en el catálogo");
        }
        Planta planta = plantaMapper.aEntidad(normalizada);
        LocalDateTime ahora = LocalDateTime.now(clock);
        planta.setActiva(true);
        planta.setCreadoEn(ahora);
        planta.setActualizadoEn(ahora);
        return plantaMapper.aDetalle(plantaRepository.save(planta));
    }

    private void validarRangoAltitud(Integer altitudMinima, Integer altitudMaxima) {
        if (altitudMinima != null && altitudMaxima != null && altitudMinima > altitudMaxima) {
            throw new ReglaNegocioException("La altitud mínima no puede ser mayor que la altitud máxima");
        }
    }

    private CrearPlantaRequest normalizar(CrearPlantaRequest request) {
        return new CrearPlantaRequest(
                request.nombreComun().strip(),
                textoOpcional(request.nombreKichwa()),
                request.nombreCientifico().strip(),
                request.familiaBotanica().strip(),
                request.cualidadTermica(),
                request.cicloCultivo(),
                request.origen(),
                request.altitudMinMsnm(),
                request.altitudMaxMsnm(),
                textoOpcional(request.regionesDistribucion()),
                request.usosMedicinales().strip(),
                textoOpcional(request.preparacionDosificacion()),
                textoOpcional(request.contraindicaciones()),
                request.categorias(),
                textoOpcional(request.fotografiaUrl()));
    }

    private String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
