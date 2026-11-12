package com.huertos.service.impl;

import com.huertos.dto.FiltrosPlantaRequest;
import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.PlantaMapper;
import com.huertos.model.Planta;
import com.huertos.repository.PlantaRepository;
import com.huertos.repository.PlantaSpecification;
import com.huertos.service.PlantaService;
import java.util.List;
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
    private static final int LONGITUD_MINIMA_CRITERIO = 2;
    private static final int LONGITUD_MAXIMA_CRITERIO = 100;
    private static final int ALTITUD_MINIMA = 0;
    private static final int ALTITUD_MAXIMA = 6000;
    private static final Sort POR_NOMBRE_COMUN = Sort.by(Sort.Direction.ASC, "nombreComun", "id");

    private final PlantaRepository plantaRepository;
    private final PlantaMapper plantaMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<PlantaResumenResponse> listarPlantasPublicas(int pagina, int tamanio) {
        PageRequest paginacion = PageRequest.of(
                Math.max(pagina, 0), Math.clamp(tamanio, TAMANIO_MINIMO, TAMANIO_MAXIMO), POR_NOMBRE_COMUN);
        return PaginaResponse.de(plantaRepository.findAllByActivaTrue(paginacion).map(plantaMapper::aResumen));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantaResumenResponse> buscarPorNombre(String criterio) {
        String criterioRecortado = validarCriterio(criterio);
        return aResumenes(plantaRepository.buscarActivasPorNombre(criterioRecortado));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantaResumenResponse> filtrarPlantas(FiltrosPlantaRequest filtros) {
        validarAltitud(filtros.altitud());
        if (filtros.criterio() != null) {
            validarCriterio(filtros.criterio());
        }
        return aResumenes(plantaRepository.findAll(PlantaSpecification.filtrar(filtros), POR_NOMBRE_COMUN));
    }

    @Override
    @Transactional(readOnly = true)
    public PlantaDetalleResponse obtenerDetalle(Long id) {
        return plantaRepository.findByIdAndActivaTrue(id)
                .map(plantaMapper::aDetalle)
                .orElseThrow(() -> new RecursoNoEncontradoException("La ficha solicitada no está disponible"));
    }

    private List<PlantaResumenResponse> aResumenes(List<Planta> plantas) {
        return plantas.stream()
                .map(plantaMapper::aResumen)
                .toList();
    }

    private void validarAltitud(Integer altitud) {
        if (altitud != null && (altitud < ALTITUD_MINIMA || altitud > ALTITUD_MAXIMA)) {
            throw new ReglaNegocioException("La altitud debe estar entre 0 y 6000 msnm");
        }
    }

    private String validarCriterio(String criterio) {
        String criterioRecortado = criterio == null ? "" : criterio.trim();
        if (criterioRecortado.length() < LONGITUD_MINIMA_CRITERIO
                || criterioRecortado.length() > LONGITUD_MAXIMA_CRITERIO) {
            throw new ReglaNegocioException("El criterio de búsqueda debe tener entre 2 y 100 caracteres");
        }
        return criterioRecortado;
    }
}
