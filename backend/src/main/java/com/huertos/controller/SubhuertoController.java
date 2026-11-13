package com.huertos.controller;

import com.huertos.dto.CrearSubhuertoRequest;
import com.huertos.dto.SubhuertoResponse;
import com.huertos.service.SubhuertoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Subhuertos", description = "Zonas de cultivo dentro del huerto comunitario")
@RestController
@RequestMapping("/api/comunidad/subhuertos")
@RequiredArgsConstructor
public class SubhuertoController {

    private final SubhuertoService subhuertoService;

    @Operation(summary = "Lista los subhuertos del huerto de la comunidad")
    @GetMapping
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ResponseEntity<List<SubhuertoResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(subhuertoService.listarPorUsuario(authentication.getName()));
    }

    @Operation(summary = "Crea un subhuerto en el huerto de la comunidad")
    @PostMapping
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public ResponseEntity<SubhuertoResponse> crear(
            Authentication authentication,
            @Valid @RequestBody CrearSubhuertoRequest request) {
        SubhuertoResponse creado = subhuertoService.crear(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}