package com.huertos.controller;

import com.huertos.dto.ActualizarComunidadRequest;
import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.service.ComunidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comunidades", description = "Gestión de comunidades y sus canales oficiales de contacto")
@RestController
@RequestMapping("/api/comunidades")
@RequiredArgsConstructor
public class ComunidadController {

    private final ComunidadService comunidadService;

    @Operation(summary = "Obtiene la ficha de la comunidad asociada al responsable autenticado")
    @GetMapping("/mi-comunidad")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ComunidadDetalleResponse obtenerMiComunidad(Authentication authentication) {
        return comunidadService.obtenerPorCorreo(authentication.getName());
    }

    @Operation(summary = "Actualiza el nombre y los canales oficiales de contacto de la comunidad")
    @PutMapping("/mi-comunidad")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ComunidadDetalleResponse actualizarMiComunidad(
            @Valid @RequestBody ActualizarComunidadRequest request,
            Authentication authentication) {
        return comunidadService.actualizarPorCorreo(authentication.getName(), request);
    }
}
