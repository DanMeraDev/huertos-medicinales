package com.huertos.controller;

import com.huertos.dto.CrearTalentoComunidadRequest;
import com.huertos.dto.TalentoComunidadResponse;
import com.huertos.service.TalentoComunidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Talentos de Comunidad", description = "Gestión de talentos e insignias de la comunidad")
@RestController
@RequestMapping("/api/comunidades/mi-comunidad/talentos")
@RequiredArgsConstructor
public class TalentoComunidadController {

    private final TalentoComunidadService talentoComunidadService;

    @Operation(summary = "Lista los talentos configurados en la comunidad del responsable autenticado")
    @GetMapping
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public List<TalentoComunidadResponse> listar(Authentication authentication) {
        return talentoComunidadService.listarPorCorreo(authentication.getName());
    }

    @Operation(summary = "Registra un nuevo talento o insignia personalizada para la comunidad")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public TalentoComunidadResponse registrar(
            @Valid @RequestBody CrearTalentoComunidadRequest request,
            Authentication authentication) {
        return talentoComunidadService.registrarTalentoPorCorreo(authentication.getName(), request);
    }
}
