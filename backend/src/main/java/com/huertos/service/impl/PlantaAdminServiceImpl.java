package com.huertos.service.impl;

import com.huertos.dto.ActualizarPlantaRequest;
import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.DarBajaPlantaRequest;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.PlantaMapper;
import com.huertos.model.Planta;
import com.huertos.repository.PlantaRepository;
import com.huertos.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final PlantaMapper plantaMapper;
    private final Clock clock;

    @Override
    @Transactional
    public PlantaDetalleResponse registrarPlanta(CrearPlantaRequest request) {
        validarRangoAltitud(request.altitudMinMsnm(), request.altitudMaxMsnm());
        Planta planta = plantaMapper.aEntidad(request);
        normalizar(planta);
        if (plantaRepository.existsByNombreCientificoIgnoreCase(planta.getNombreCientifico())) {
            throw new ConflictoException("El nombre científico ya existe en el catálogo");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        planta.setActiva(true);
        planta.setCreadoEn(ahora);
        planta.setActualizadoEn(ahora);
        return plantaMapper.aDetalle(plantaRepository.save(planta));
    }

    @Override
    @Transactional
    public PlantaDetalleResponse actualizarPlanta(Long id, ActualizarPlantaRequest request, String correo) {
        validarRangoAltitud(request.altitudMinMsnm(), request.altitudMaxMsnm());
        Planta planta = plantaRepository.findByIdAndActivaTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La planta no existe o fue dada de baja"));
        if (plantaRepository.existsByNombreCientificoIgnoreCaseAndIdNot(request.nombreCientifico().strip(), id)) {
            throw new ConflictoException("Nombre científico ya registrado en otra planta");
        }
        plantaMapper.actualizarEntidad(request, planta);
        normalizar(planta);
        planta.setActualizadoPor(usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario")));
        planta.setActualizadoEn(LocalDateTime.now(clock));
        return plantaMapper.aDetalle(plantaRepository.save(planta));
    }

    @Override
    @Transactional
    public void darDeBajaPlanta(Long id, DarBajaPlantaRequest request, String correo) {
    }

    private void validarRangoAltitud(Integer altitudMinima, Integer altitudMaxima) {
        if (altitudMinima != null && altitudMaxima != null && altitudMinima > altitudMaxima) {
            throw new ReglaNegocioException("La altitud mínima no puede ser mayor que la altitud máxima");
        }
    }

    private void normalizar(Planta planta) {
        planta.setNombreComun(planta.getNombreComun().strip());
        planta.setNombreKichwa(textoOpcional(planta.getNombreKichwa()));
        planta.setNombreCientifico(planta.getNombreCientifico().strip());
        planta.setFamiliaBotanica(planta.getFamiliaBotanica().strip());
        planta.setRegionesDistribucion(textoOpcional(planta.getRegionesDistribucion()));
        planta.setUsosMedicinales(planta.getUsosMedicinales().strip());
        planta.setPreparacionDosificacion(textoOpcional(planta.getPreparacionDosificacion()));
        planta.setContraindicaciones(textoOpcional(planta.getContraindicaciones()));
        planta.setFotografiaUrl(textoOpcional(planta.getFotografiaUrl()));
    }

    private String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
