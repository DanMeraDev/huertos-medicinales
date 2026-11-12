package com.huertos.controller;

import com.huertos.dto.ActualizarHuertoRequest;
import com.huertos.dto.HuertoDetalleResponse;
import com.huertos.service.HuertoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comunidad/huerto")
@RequiredArgsConstructor
@Tag(name = "Huerto", description = "Consulta y configuración operativa del huerto comunitario")
public class HuertoController {

    private final HuertoService huertoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_COMUNIDAD', 'TECNICO_MSP')")
    @Operation(summary = "Obtiene la configuración operativa actual del huerto")
    public ResponseEntity<HuertoDetalleResponse> obtenerHuerto(Authentication authentication) {
        return ResponseEntity.ok(huertoService.obtenerPorCorreo(authentication.getName()));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE_COMUNIDAD', 'TECNICO_MSP')")
    @Operation(summary = "Actualiza los datos operativos y agronómicos del huerto")
    public ResponseEntity<HuertoDetalleResponse> actualizarHuerto(
            Authentication authentication,
            @Valid @RequestBody ActualizarHuertoRequest request) {
        return ResponseEntity.ok(huertoService.actualizarPorCorreo(authentication.getName(), request));
    }
}