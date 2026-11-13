package com.huertos.controller;

import com.huertos.dto.BajaMaterialResponse;
import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarBajaMaterialRequest;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import com.huertos.service.BodegaService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bodega", description = "Gestión del material vegetal y existencias de bodega comunitaria")
@RestController
@RequestMapping("/api/bodega")
@RequiredArgsConstructor
public class BodegaController {

    private final BodegaService bodegaService;

    @Operation(summary = "Registra la baja de material vegetal con causa de fracaso")
    @PostMapping("/material-vegetal/{id}/bajas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public BajaMaterialResponse registrarBajaMaterial(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarBajaMaterialRequest request,
            Authentication authentication) {
        return bodegaService.registrarBajaMaterial(id, request, authentication.getName());
    }

    @Operation(summary = "Registra el ingreso de nuevo material vegetal a la bodega comunitaria")
    @PostMapping("/material-vegetal")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public MaterialVegetalResponse registrarIngresoMaterial(
            @Valid @RequestBody RegistrarMaterialVegetalRequest request,
            Authentication authentication) {
        return bodegaService.registrarIngresoMaterial(
                bodegaService.obtenerComunidadIdPorCorreo(authentication.getName()), request);
    }

    @Operation(summary = "Consulta el inventario de material vegetal disponible en la bodega de una comunidad")
    @GetMapping("/comunidades/{comunidadId}/material-vegetal")
    @PreAuthorize("hasAnyRole('RESPONSABLE_COMUNIDAD', 'TECNICO_MSP')")
    public List<MaterialVegetalResponse> listarMaterialPorComunidad(
            @PathVariable Long comunidadId,
            Authentication authentication) {
        boolean esTecnico = authentication.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_TECNICO_MSP"));
        bodegaService.validarAccesoComunidad(comunidadId, authentication.getName(), esTecnico);
        return bodegaService.listarMaterialPorComunidad(comunidadId);
    }
}
