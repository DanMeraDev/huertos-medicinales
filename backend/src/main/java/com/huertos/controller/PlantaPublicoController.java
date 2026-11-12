package com.huertos.controller;

import com.huertos.dto.FiltrosPlantaRequest;
import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.service.PlantaService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico/plantas")
@RequiredArgsConstructor
@Tag(name = "Catálogo público de plantas")
public class PlantaPublicoController {

    private final PlantaService plantaService;

    @GetMapping
    @Operation(summary = "Lista las plantas activas del catálogo ordenadas por nombre común")
    public PaginaResponse<PlantaResumenResponse> listarPlantas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return plantaService.listarPlantasPublicas(page, size);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca plantas activas por nombre común, científico o kichwa")
    public List<PlantaResumenResponse> buscarPlantas(@RequestParam String criterio) {
        return plantaService.buscarPorNombre(criterio);
    }

    @GetMapping("/filtrar")
    @Operation(summary = "Filtra plantas activas por cualidad, altitud, región, categoría, ciclo, origen y nombre")
    public List<PlantaResumenResponse> filtrarPlantas(@ParameterObject FiltrosPlantaRequest filtros) {
        return plantaService.filtrarPlantas(filtros);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene la ficha técnica de una planta activa del catálogo")
    public PlantaDetalleResponse obtenerDetalle(@PathVariable Long id) {
        return plantaService.obtenerDetalle(id);
    }
}
