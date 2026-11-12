package com.huertos.controller;

import com.huertos.dto.DirectorioComunidadResponse;
import com.huertos.service.ComunidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico/comunidades")
@RequiredArgsConstructor
@Tag(name = "Comunidades Público", description = "Endpoints de consulta pública para el directorio territorial de comunidades")
public class ComunidadPublicoController {

    private final ComunidadService comunidadService;

    @GetMapping
    @Operation(summary = "Obtiene el directorio público de comunidades activas con sus vías de contacto y ubicación")
    public List<DirectorioComunidadResponse> obtenerDirectorio() {
        return comunidadService.obtenerDirectorioPublico();
    }
}
