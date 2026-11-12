package com.huertos.controller;

import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.service.PlantaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
