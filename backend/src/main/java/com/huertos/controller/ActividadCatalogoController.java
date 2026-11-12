package com.huertos.controller;

import com.huertos.dto.ActividadCatalogoResponse;
import com.huertos.dto.CrearActividadCatalogoRequest;
import com.huertos.service.ActividadCatalogoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comunidad/actividades-catalogo")
@RequiredArgsConstructor
public class ActividadCatalogoController {

    private final ActividadCatalogoService actividadCatalogoService;

    @GetMapping
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ResponseEntity<List<ActividadCatalogoResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(actividadCatalogoService.listarPorResponsable(authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ResponseEntity<ActividadCatalogoResponse> crear(
            Authentication authentication,
            @Valid @RequestBody CrearActividadCatalogoRequest request) {
        ActividadCatalogoResponse creada = actividadCatalogoService.crear(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}