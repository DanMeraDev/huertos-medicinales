package com.huertos.controller;

import com.huertos.dto.RegistrarSiembraRequest;
import com.huertos.dto.SiembraDetalleResponse;
import com.huertos.service.SiembraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comunidad/siembras")
@RequiredArgsConstructor
@Tag(name = "Siembras", description = "Gestión de siembras en subhuertos comunitarios")
public class SiembraController {

    private final SiembraService siembraService;

    @PostMapping
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    @Operation(summary = "Registra una nueva siembra en un subhuerto")
    public ResponseEntity<SiembraDetalleResponse> registrarSiembra(
            Authentication authentication,
            @Valid @RequestBody RegistrarSiembraRequest request) {
        SiembraDetalleResponse respuesta = siembraService.registrarSiembra(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}

