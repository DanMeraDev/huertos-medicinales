package com.huertos.controller;

import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import com.huertos.service.MiembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Miembros de Comunidad", description = "Gestión de miembros y voluntarios de las comunidades")
@RestController
@RequestMapping("/api/comunidades/mi-comunidad/miembros")
@RequiredArgsConstructor
public class ComunidadMiembroController {

    private final MiembroService miembroService;

    @Operation(summary = "Registra un nuevo miembro voluntario en la comunidad o vincula uno existente")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public MiembroDetalleResponse registrarMiembro(
            @Valid @RequestBody RegistrarMiembroRequest request,
            Authentication authentication) {
        return miembroService.registrarMiembroPorCorreo(authentication.getName(), request);
    }
}
