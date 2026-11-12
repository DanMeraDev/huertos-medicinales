package com.huertos.controller;

import com.huertos.dto.SaberAncestralDetalleResponse;
import com.huertos.dto.SaberAncestralResumenResponse;
import com.huertos.service.SaberAncestralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Saberes ancestrales")
@RestController
@RequestMapping("/api/publico/saberes-ancestrales")
@RequiredArgsConstructor
public class SaberAncestralPublicoController {

    private final SaberAncestralService saberAncestralService;

    @Operation(summary = "Devuelve la ruta secuencial de saberes ancestrales")
    @GetMapping
    public List<SaberAncestralResumenResponse> listarRutaSaberes() {
        return saberAncestralService.listarRutaSaberes();
    }

    @Operation(summary = "Devuelve el detalle de un saber ancestral")
    @GetMapping("/{id}")
    public SaberAncestralDetalleResponse obtenerDetalleSaber(@PathVariable Long id) {
        return saberAncestralService.obtenerPorId(id);
    }
}
