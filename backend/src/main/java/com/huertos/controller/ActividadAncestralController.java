package com.huertos.controller;

import com.huertos.dto.ActividadAncestralResponse;
import com.huertos.dto.ActividadParaCorregirResponse;
import com.huertos.dto.RegistrarActividadAncestralRequest;
import com.huertos.dto.ReenviarEvidenciasRequest;
import com.huertos.service.ActividadAncestralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Actividades Ancestrales MSP", description = "Registro de actividades ancestrales grandes con evidencias para revisión del MSP")
@RestController
@RequestMapping("/api/actividades-ancestrales")
@RequiredArgsConstructor
public class ActividadAncestralController {

    private final ActividadAncestralService actividadAncestralService;

    @Operation(summary = "Registra una actividad ancestral grande con evidencias y participantes para revisión del MSP")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ActividadAncestralResponse registrarActividadGrande(
            @Valid @RequestBody RegistrarActividadAncestralRequest request,
            Authentication authentication) {
        return actividadAncestralService.registrarActividadGrande(authentication.getName(), request);
    }

    @Operation(summary = "Obtiene la actividad ancestral observada con la observación del Técnico MSP para corregirla")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ActividadParaCorregirResponse buscarActividadParaCorregir(
            @PathVariable Long id,
            Authentication authentication) {
        return actividadAncestralService.buscarParaCorregir(authentication.getName(), id);
    }

    @Operation(summary = "Lista las actividades de la comunidad en estado OBSERVADA_CORREGIR para corregirlas")
    @GetMapping("/observadas")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public List<ActividadParaCorregirResponse> listarObservadas(Authentication authentication) {
        return actividadAncestralService.listarObservadas(authentication.getName());
    }

    @Operation(summary = "Reenvía las evidencias corregidas de una actividad ancestral observada")
    @PutMapping("/{id}/reenviar")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ActividadAncestralResponse reenviarEvidencias(
            @PathVariable Long id,
            @Valid @RequestBody ReenviarEvidenciasRequest request,
            Authentication authentication) {
        return actividadAncestralService.reenviarEvidencias(authentication.getName(), id, request);
    }
}
