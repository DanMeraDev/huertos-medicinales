package com.huertos.controller;

import com.huertos.dto.PerfilPublicoMiembroResponse;
import com.huertos.service.MiembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico/miembros")
@RequiredArgsConstructor
@Tag(name = "Miembros Público", description = "Consulta pública del perfil motivacional de voluntarios por cédula")
public class MiembroPublicoController {

    private final MiembroService miembroService;

    @GetMapping("/cedula/{cedula}")
    @Operation(summary = "Consulta el perfil público y vitrina de insignias de un miembro activo por cédula")
    public PerfilPublicoMiembroResponse obtenerPerfilPorCedula(
            @PathVariable
            @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe contener exactamente 10 dígitos numéricos")
            String cedula) {
        return miembroService.obtenerPerfilPublicoPorCedula(cedula);
    }
}
