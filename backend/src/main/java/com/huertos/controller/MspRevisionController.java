package com.huertos.controller;

import com.huertos.dto.ActividadRevisionResponse;
import com.huertos.dto.ResultadoVerificacionResponse;
import com.huertos.dto.VerificarActividadMspRequest;
import com.huertos.service.MspRevisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Revisión de actividades ancestrales", description = "Bandeja de auditoría y dictámenes del Técnico MSP")
@RestController
@RequestMapping("/api/msp/actividades-ancestrales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TECNICO_MSP')")
public class MspRevisionController {

    private final MspRevisionService mspRevisionService;

    @GetMapping("/pendientes")
    @Operation(summary = "Lista las actividades ancestrales pendientes de verificación con sus evidencias")
    public List<ActividadRevisionResponse> listarPendientes() {
        return mspRevisionService.listarPendientes();
    }

    @PostMapping("/{id}/verificar")
    @Operation(summary = "Emite el dictamen del Técnico MSP sobre una actividad ancestral pendiente")
    public ResultadoVerificacionResponse verificarActividad(
            @PathVariable Long id,
            @Valid @RequestBody VerificarActividadMspRequest request,
            Authentication authentication) {
        return mspRevisionService.dictaminarActividad(authentication.getName(), id, request);
    }
}
